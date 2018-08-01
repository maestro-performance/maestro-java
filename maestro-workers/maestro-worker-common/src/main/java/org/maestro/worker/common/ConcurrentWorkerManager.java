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
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.duration.DurationDrain;
import org.maestro.common.evaluators.HardLatencyEvaluator;
import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.evaluators.SoftLatencyEvaluator;
import org.maestro.common.worker.*;
import org.maestro.worker.common.container.initializers.TestWorkerInitializer;
import org.maestro.worker.common.ds.MaestroDataServer;
import org.maestro.worker.common.watchdog.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A specialized worker manager that can manage concurrent test workers (ie.: senders/receivers)
 */
public class ConcurrentWorkerManager extends MaestroWorkerManager implements MaestroReceiverEventListener,MaestroSenderEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentWorkerManager.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final WorkerContainer container;
    private final Class<MaestroWorker> workerClass;
    private final File logDir;
    private LatencyEvaluator latencyEvaluator;

    /**
     * Constructor
     * @param maestroURL Maestro URL
     * @param role Worker Role
     * @param host hostname for the worker
     * @param logDir test log directory
     * @param workerClass the class for the worker
     * @param dataServer the data server instance
     */
    public ConcurrentWorkerManager(final String maestroURL, final String role, final String host, final File logDir,
                                   final Class<MaestroWorker> workerClass, final MaestroDataServer dataServer) {
        super(maestroURL, role, host, dataServer);

        this.container = new WorkerContainer();
        this.workerClass = workerClass;
        this.logDir = logDir;
    }


    /**
     * Starts the workers and add them to a container
     * @return true if started correctly or false otherwise
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

            final TestWorkerInitializer testWorkerInitializer = new TestWorkerInitializer(workerClass, latencyEvaluator,
                    getWorkerOptions());

            int count = getWorkerOptions().getParallelCountAsInt();
            logger.debug("Creating {} workers of type {}", count, workerClass);
            final List<MaestroWorker> workers = container.create(testWorkerInitializer, count);

            if (workers.isEmpty()) {
                logger.warn("No workers were created");

                return false;
            }

            logger.debug("Removing previous observers");
            container.getObservers().clear();

            if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
                logger.debug("Setting up the observer: worker latency writer");
                WorkerLatencyWriter latencyWriter = getLatencyWriter(testLogDir, workers);
                container.getObservers().add(new LatencyWriterObserver(latencyWriter));
            }

            logger.debug("Setting up the observer: worker rate writer");
            WorkerRateWriter workerRateWriter = new WorkerRateWriter(testLogDir, workers);
            container.getObservers().add(new RateWriterObserver(workerRateWriter));

            // Note: it uses the base log dir because of the symlinks
            logger.debug("Setting up the observer: worker shutdown observer");
            WorkerShutdownObserver workerShutdownObserver = new WorkerShutdownObserver(logDir, getClient());
            container.getObservers().add(workerShutdownObserver);


            if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
                logger.debug("Setting up the observer: drain observer");
                container.getObservers().add(new DrainObserver(getWorkerOptions(), testWorkerInitializer, getClient()));
            }

            logger.debug("Setting up the observer: worker cleanup observer");
            container.getObservers().add(new CleanupObserver());

            logger.debug("Starting the workers {}", workerClass);

            container.start(latencyEvaluator);

            getClient().replyOk();
            return true;
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            getClient().replyInternalError();
        }

        return false;
    }

    private WorkerLatencyWriter getLatencyWriter(File testLogDir, List<MaestroWorker> workers) {

        if (latencyEvaluator == null) {
            return new WorkerLatencyWriter(testLogDir, workers);
        }
        else {
            long reportingInterval = config.getLong("maestro.worker.reporting.interval", 10000);
            return new WorkerLatencyWriter(testLogDir, workers, latencyEvaluator,
                    reportingInterval);
        }
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

    private void shutdownAndWaitWriters() {
        this.latencyEvaluator = null;
    }

    @Override
    public void handle(SetRequest note) {
        super.handle(note);

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

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StartSender note) {
        logger.info("Start sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StopReceiver note) {
        logger.info("Stop receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        getClient().replyOk();
    }

    @Override
    public void handle(StopSender note) {
        logger.info("Stop sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

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

    private boolean drainStart() {
        WorkerOptions drainOptions = new WorkerOptions();

        drainOptions.setBrokerURL(getWorkerOptions().getBrokerURL());
        drainOptions.setParallelCount(getWorkerOptions().getParallelCount());
        drainOptions.setDuration(DurationDrain.DURATION_DRAIN_FORMAT);

        return drainStart(drainOptions);
    }

    private boolean drainStart(WorkerOptions drainOptions) {
        if (container.isTestInProgress()) {
            logger.warn("Trying to start draining the SUT, but a test execution is already in progress");
            getClient().notifyFailure("Test in progress");

            return false;
        }

        try {
            final List<MaestroWorker> workers = new ArrayList<>();

            logger.debug("Starting the workers {}", workerClass);
            WorkerOptions wo = new WorkerOptions();

            container.getObservers().clear();

            logger.debug("Setting up the observer: worker cleanup observer");
            container.getObservers().add(new CleanupObserver());

            container.start(null);

            if (workers.isEmpty()) {
                logger.warn("No workers has been started!");
            } else {
                logger.debug("Creating the latency writer thread");

                //TODO handle shutdown gently
                Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownAndWaitWriters));
            }

            final int drainRetries = (config.getInt("worker.auto.drain.retries", 10) + 5);

            container.waitForComplete(drainRetries * 1000);

            return true;
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            getClient().replyInternalError();
        }

        return false;
    }

    @Override
    public void handle(DrainRequest note) {
        WorkerOptions drainOptions = new WorkerOptions();

        drainOptions.setBrokerURL(note.getUrl());
        drainOptions.setDuration(note.getDuration());
        drainOptions.setParallelCount(note.getParallelCount());

        if (!drainStart(drainOptions)) {
            logger.error("Unable to start draining from the SUT");
        }
    }


}
