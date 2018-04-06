/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.worker.base;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.evaluators.HardLatencyEvaluator;
import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.evaluators.SoftLatencyEvaluator;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.*;
import org.maestro.worker.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.maestro.worker.base.WorkerStateInfoUtil.isCleanExit;

/**
 * A specialized worker manager that can manage concurrent test workers (ie.: senders/receivers)
 */
public class ConcurrentWorkerManager extends MaestroWorkerManager implements MaestroReceiverEventListener,MaestroSenderEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentWorkerManager.class);
    private static final long TIMEOUT_STOP_WORKER_MILLIS = 1_000;
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final WorkerContainer container;
    private final Class<MaestroWorker> workerClass;
    private final File logDir;
    private Thread latencyWriterThread;
    private Thread rateWriterThread;
    private LatencyEvaluator latencyEvaluator;

    /**
     * Constructor
     * @param maestroURL
     * @param role
     * @param host
     * @param logDir
     * @param workerClass
     * @throws MaestroException
     */
    public ConcurrentWorkerManager(final String maestroURL, final String role, final String host, final File logDir,
                                   final Class<MaestroWorker> workerClass, final MaestroDataServer dataServer) throws MaestroException {
        super(maestroURL, role, host, dataServer);

        this.container = WorkerContainer.getInstance(getClient());
        this.workerClass = workerClass;
        this.logDir = logDir;
    }


    /**
     * Starts the workers and add them to a container
     * @return
     */
    private boolean doWorkerStart() {
        if (container.isTestInProgress()) {
            logger.warn("Trying to start a new test, but a test execution is already in progress");
            getClient().notifyFailure("Test already in progress");

            return false;
        }

        final File testLogDir = TestLogUtils.nextTestLogDir(logDir);

        setupLatencyEvaluator();

        try {
            super.writeTestProperties(testLogDir);

            final List<MaestroWorker> workers = new ArrayList<>();

            logger.debug("Starting the workers {}", workerClass);
            container.start(workerClass, workers, this::onStoppedWorkers, latencyEvaluator);

            if (workers.isEmpty()) {
                logger.warn("No workers has been started!");
            } else {
                logger.debug("Creating the latency writer thread");

                WorkerLatencyWriter latencyWriter;
                if (latencyEvaluator == null) {
                    latencyWriter = new WorkerLatencyWriter(testLogDir, workers);
                }
                else {
                    long reportingInterval = config.getLong("maestro.worker.reporting.interval", 10000);
                    latencyWriter = new WorkerLatencyWriter(testLogDir, workers, latencyEvaluator,
                            reportingInterval);
                }
                this.latencyWriterThread = new Thread(latencyWriter);

                logger.debug("Creating the rate writer thread");
                WorkerChannelWriter rateWriter = new WorkerChannelWriter(testLogDir, workers);
                this.rateWriterThread = new Thread(rateWriter);

                logger.debug("Starting the writers threads");
                this.latencyWriterThread.start();
                this.rateWriterThread.start();

                //TODO handle shutdown gently
                Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownAndWaitWriters));
            }

            getClient().replyOk();
            return true;
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            getClient().replyInternalError();
        }

        return false;
    }


    private void setupLatencyEvaluator() {
        Double givenLatency = super.getWorkerOptions().getFclAsDouble();
        if (givenLatency == null) {
            return;
        }

        String policy = config.getString("maestro.worker.fcl.default.policy", "soft");

        if (policy.equals("soft")) {
            double defaultPercentile = config.getDouble("maestro.worker.fcl.soft.percentile", 90.0);

            logger.debug("Setting max latency to {} if at percentile {}", givenLatency, defaultPercentile);

            // The latency comes as milliseconds from the front-end
            this.latencyEvaluator = new SoftLatencyEvaluator(givenLatency * 1000, defaultPercentile);
        }
        else {
            logger.debug("Setting max latency to {}", givenLatency);

            // The latency comes as milliseconds from the front-end
            this.latencyEvaluator = new HardLatencyEvaluator(givenLatency * 1000);
        }
    }

    private void shutdownAndWaitWriters(){
        if (this.rateWriterThread != null) {
            this.rateWriterThread.interrupt();
        }
        if (this.latencyWriterThread != null) {
            this.latencyWriterThread.interrupt();
        }
        if (this.rateWriterThread != null) {
            try {
                this.rateWriterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.latencyWriterThread != null) {
            try {
                this.latencyWriterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean awaitWorkers(long startWaitingWorkersEpochMillis, final List<WorkerRuntimeInfo> workers) {
        final long deadLine = startWaitingWorkersEpochMillis + (workers.size() * TIMEOUT_STOP_WORKER_MILLIS * 2);
        //workers are being stopped, just need to check if they have finished their jobs
        boolean allFinished = false;
        while (!allFinished && System.currentTimeMillis() < deadLine) {
            allFinished = true;
            for (int i = 0, size = workers.size(); i < size; i++) {
                final WorkerRuntimeInfo workerRuntimeInfo = workers.get(i);
                try {
                    workerRuntimeInfo.thread.join(TIMEOUT_STOP_WORKER_MILLIS);
                } catch (InterruptedException e) {
                    //no op, just retry
                } finally {
                    allFinished = !workerRuntimeInfo.thread.isAlive();
                    if (!allFinished) {
                        break;
                    }
                }
            }
        }
        return allFinished;
    }

    /**
     * It should be called by a different thread/concurrently too (eg WatchDog) so it can't modify any this.* members or workers too.
     */
    private void onStoppedWorkers(List<WorkerRuntimeInfo> workers) {
        try {
            if (this.rateWriterThread != null || this.latencyWriterThread != null) {
                final long startWaitingWorkers = System.currentTimeMillis();
                if (!workers.isEmpty()) {
                    final boolean allFinished = awaitWorkers(startWaitingWorkers, workers);
                    if (!allFinished) {
                        logger.warn("The writer will be forced to stop with alive workers");
                    }
                }

                shutdownAndWaitWriters();
                final long elapsedMillis = System.currentTimeMillis() - startWaitingWorkers;
                logger.info("Awaiting workers and shutting down writers took {} ms", elapsedMillis);
            }

            boolean failed = false;
            for (WorkerRuntimeInfo ri : workers) {
                WorkerStateInfo wsi = ri.worker.getWorkerState();

                if (!isCleanExit(wsi)) {
                    failed = true;
                    break;
                }
            }

            TestLogUtils.createSymlinks(logDir, failed);
        } finally {
            //reset it for new incoming tests
            setWorkerOptions(new WorkerOptions());
        }
    }

    @Override
    public void handle(SetRequest note) {
        super.handle(note);
        container.setWorkerOptions(getWorkerOptions());

        getClient().replyOk();
    }

    @Override
    public void handle(Halt note) {
        logger.debug("Halt request received");

        container.stop();
        setRunning(false);
    }

    @Override
    public void handle(StartReceiver note) {
        logger.debug("Start receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StartSender note) {
        logger.debug("Start sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StopReceiver note) {
        logger.debug("Stop receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        getClient().replyOk();
    }

    @Override
    public void handle(StopSender note) {
        logger.debug("Stop sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        getClient().replyOk();
    }


    @Override
    public void handle(StatsRequest note) {
        logger.debug("Stats request received");
        StatsResponse statsResponse = new StatsResponse();

        String parallelCount = getWorkerOptions().getParallelCount();

        if (parallelCount == null) {
            statsResponse.setChildCount(0);
        }
        else {
            statsResponse.setChildCount(Integer.parseInt(parallelCount));
        }

        // Explanation: the role is the name as the role (ie: clientName@host)
        statsResponse.setRole(getClientName());

        LatencyStats latencyStats = container.latencyStats();
        if (latencyStats != null) {
            statsResponse.setLatency(latencyStats.getLatency());
        }
        else {
            statsResponse.setLatency(0);
        }

        ThroughputStats throughputStats = container.throughputStats();

        if (throughputStats != null) {
            statsResponse.setRate(throughputStats.getRate());
            statsResponse.setCount(throughputStats.getCount());
        }
        else {
            statsResponse.setRate(0);
            statsResponse.setCount(0);
        }
        statsResponse.setRoleInfo("");
        statsResponse.setTimestamp("0");

        getClient().statsResponse(statsResponse);
    }
}
