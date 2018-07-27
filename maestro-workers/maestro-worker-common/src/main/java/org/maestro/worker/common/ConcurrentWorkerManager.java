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

package org.maestro.worker.common;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.evaluators.HardLatencyEvaluator;
import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.evaluators.SoftLatencyEvaluator;
import org.maestro.common.worker.*;
import org.maestro.worker.common.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.maestro.worker.common.WorkerStateInfoUtil.isCleanExit;

/**
 * A specialized worker manager that can manage concurrent test workers (ie.: senders/receivers)
 */
public class ConcurrentWorkerManager extends MaestroWorkerManager implements MaestroReceiverEventListener,MaestroSenderEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentWorkerManager.class);
    private static final long TIMEOUT_STOP_WORKER_MILLIS;
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final WorkerContainer container;
    private final File logDir;
    private Thread latencyWriterThread;
    private Thread rateWriterThread;
    private LatencyEvaluator latencyEvaluator;

    private final HashMap<String, String> workersMap = new HashMap<>();

    static {
        TIMEOUT_STOP_WORKER_MILLIS = config.getLong("maestro.worker.stop.timeout", 1000);
    }

    /**
     * Constructor
     * @param maestroURL Maestro URL
     * @param role Worker Role
     * @param host hostname for the worker
     * @param logDir test log directory
     * @param dataServer the data server instance
     */
    public ConcurrentWorkerManager(final String maestroURL, final String role, final String host, final File logDir,
                                   final MaestroDataServer dataServer) {
        super(maestroURL, role, host, dataServer);

        this.container = WorkerContainer.getInstance(getClient());

        workersMap.put("JmsSender", "org.maestro.worker.jms.JMSSenderWorker");
        workersMap.put("JmsReceiver", "org.maestro.worker.jms.JMSReceiverWorker");

        this.logDir = logDir;
    }


    /**
     * Starts the workers and add them to a container
     * @return true if started correctly or false otherwise
     */
    private boolean doWorkerStart(final Class<MaestroWorker> workerClass) {
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
                logger.error("Rate writer thread was interrupted: {}", e.getMessage(), e);
            }
        }
        if (this.latencyWriterThread != null) {
            try {
                this.latencyWriterThread.join();
            } catch (InterruptedException e) {
                logger.error("Latency writer thread was interrupted: {}", e.getMessage(), e);
            }
        }

        this.latencyEvaluator = null;
    }

    private static long awaitWorkers(long startWaitingWorkersEpochMillis, final List<WorkerRuntimeInfo> workers) {
        if (workers.isEmpty()) {
            return 0;
        }

        int runningCount = workers.size();
        final long deadLine = startWaitingWorkersEpochMillis + (runningCount * TIMEOUT_STOP_WORKER_MILLIS * 2);

        // workers are being stopped, just need to check if they have finished their jobs
        long activeThreads = runningCount;
        while (activeThreads > 0 && System.currentTimeMillis() < deadLine) {
            workers.stream()
                    .filter(workerRuntimeInfo -> workerRuntimeInfo.thread.isAlive())
                    .parallel()
                    .forEach(workerRuntimeInfo -> finishWorkers(workerRuntimeInfo));

            activeThreads = workers.stream()
                    .filter(workerRuntimeInfo -> workerRuntimeInfo.thread.isAlive())
                    .count();

            logger.info("There are {} workers threads still alive", activeThreads);
        }

        return activeThreads;
    }

    private static void finishWorkers(final WorkerRuntimeInfo workerRuntimeInfo) {
        try {
            logger.debug("Waiting for worker thread {}", workerRuntimeInfo.thread.getId());
            workerRuntimeInfo.thread.join(TIMEOUT_STOP_WORKER_MILLIS);
        } catch (InterruptedException e) {
            //no op, just retry
        } finally {
            if (workerRuntimeInfo.thread.isAlive()) {
                logger.debug("Worker thread {} is still alive", workerRuntimeInfo.thread.getId());
            }
        }
    }

    /**
     * It should be called by a different thread/concurrently too (eg WatchDog) so it can't modify any this.* members or workers too.
     */
    private void onStoppedWorkers(List<WorkerRuntimeInfo> workers) {
        boolean failed = false;
        String exceptionMessage = null;

        try {
            if (this.rateWriterThread != null || this.latencyWriterThread != null) {
                final long startWaitingWorkers = System.currentTimeMillis();
                if (awaitWorkers(startWaitingWorkers, workers) > 0) {
                    logger.warn("The writer will be forced to stop with alive workers");
                }

                shutdownAndWaitWriters();
                final long elapsedMillis = System.currentTimeMillis() - startWaitingWorkers;
                logger.info("Awaiting workers and shutting down writers took {} ms", elapsedMillis);
            }

            for (WorkerRuntimeInfo ri : workers) {
                WorkerStateInfo wsi = ri.worker.getWorkerState();

                if (ri.thread != null && ri.thread.isAlive()) {
                    logger.warn("Worker {} is reportedly still alive", ri.thread.getId());
                    ri.thread.interrupt();
                }

                if (!isCleanExit(wsi)) {
                    failed = true;
                    exceptionMessage = Objects.requireNonNull(wsi.getException()).getMessage();

                    break;
                }
            }
        } finally {
            TestLogUtils.createSymlinks(logDir, failed);

            sendTestNotification(failed, exceptionMessage);

            setWorkerOptions(new WorkerOptions());

            getClient().unsubscribe(MaestroTopics.RECEIVER_DAEMONS);

            final String topicByName = MaestroTopics.peerTopic(getClientName(), getClient().getHost());
            getClient().unsubscribe(topicByName);

            final String topicById = MaestroTopics.peerTopic(getId());
            getClient().unsubscribe(topicById);

            workers.clear();
        }
    }

    private void sendTestNotification(boolean failed, String exceptionMessage) {

        if (failed) {
            if (exceptionMessage != null) {
                getClient().notifyFailure(exceptionMessage);
            }
            else {
                getClient().notifyFailure("Unhandled worker error");
            }
        }
        else {
            getClient().notifySuccess("Test completed successfully");
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
        logger.info("Start receiver request received");

        String workerName = note.getWorkerName();
        String workerClassName = workersMap.get(workerName);

        if (workerClassName == null) {
            logger.error("Unknown worker class");
            getClient().replyInternalError();
        }

        try {
            Class<MaestroWorker> workerClass = (Class<MaestroWorker>) Class.forName(workerClassName);

            if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
                if (!doWorkerStart(workerClass)) {
                    logger.warn("Cannot start worker {}", workerClassName);
                }
                else {
                    getClient().subscribe(MaestroTopics.RECEIVER_DAEMONS, 0);

                    final String topicByName = MaestroTopics.peerTopic(getClientName(), getClient().getHost());
                    getClient().subscribe(topicByName, 0);

                    final String topicById = MaestroTopics.peerTopic(getId());
                    getClient().subscribe(topicById, 0);
                }
            }

        } catch (ClassNotFoundException e) {
            logger.error("Class not found: {}", e.getMessage(), e);
            getClient().replyInternalError();
        }
    }

    @Override
    public void handle(StartSender note) {
        logger.info("Start sender request received");

        String workerName = note.getWorkerName();
        String workerClassName = workersMap.get(workerName);

        if (workerClassName == null) {
            logger.error("Unknown worker class");
            getClient().replyInternalError();
        }

        try {
            Class<MaestroWorker> workerClass = (Class<MaestroWorker>) Class.forName(workerClassName);

            if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
                if (!doWorkerStart(workerClass)) {
                    logger.warn("Cannot start worker {}", workerClassName);
                }
                else {
                    getClient().subscribe(MaestroTopics.SENDER_DAEMONS, 0);

                    final String topicByName = MaestroTopics.peerTopic(getClientName(), getClient().getHost());
                    getClient().subscribe(topicByName, 0);

                    final String topicById = MaestroTopics.peerTopic(getId());
                    getClient().subscribe(topicById, 0);
                }
            }

        } catch (ClassNotFoundException e) {
            logger.error("Class not found: {}", e.getMessage(), e);
            getClient().replyInternalError();
        }


    }

    @Override
    public void handle(StopWorker note) {
        logger.info("Stop receiver request received");

        container.stop();
        getClient().replyOk();
    }


    @Override
    public void handle(StatsRequest note) {
        logger.trace("Stats request received");
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

    @Override
    public void handle(final TestFailedNotification note) {
        super.handle(note);

        logger.debug("Stopping test execution after a peer reported a test failure");
        container.stop();
    }

    @Override
    public void handle(final TestSuccessfulNotification note) {
        super.handle(note);

        logger.debug("Stopping test execution after a peer reported a test success");
        container.stop();
    }

    @Override
    public void handle(final LogRequest note) {
        if (container.isTestInProgress()) {
            logger.warn("Request a log but a test execution is in progress. Files will be from older tests");
        }

        super.handle(note, logDir);
    }
}
