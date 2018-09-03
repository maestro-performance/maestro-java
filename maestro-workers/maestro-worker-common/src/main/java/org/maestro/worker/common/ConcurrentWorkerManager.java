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
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Role;
import org.maestro.common.client.notes.MaestroNote;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * A specialized worker manager that can manage concurrent test workers (ie.: senders/receivers)
 */
public class ConcurrentWorkerManager extends MaestroWorkerManager implements MaestroReceiverEventListener,MaestroSenderEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentWorkerManager.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final WorkerContainer container;
    private final File logDir;
    private LatencyEvaluator latencyEvaluator;
    private final Map<String, String> workersMap = new HashMap<>();

    /**
     * Constructor
     * @param maestroURL Maestro URL
     * @param peerInfo Peer information
     * @param logDir test log directory
     * @param dataServer the data server instance
     */
    public ConcurrentWorkerManager(final String maestroURL, final PeerInfo peerInfo, final File logDir,
                                   final MaestroDataServer dataServer) {
        super(maestroURL, peerInfo, dataServer);

        this.container = new WorkerContainer();
        this.logDir = logDir;

        workersMap.put("JmsSender", "org.maestro.worker.jms.JMSSenderWorker");
        workersMap.put("JmsReceiver", "org.maestro.worker.jms.JMSReceiverWorker");
    }


    /**
     * Starts the workers and add them to a container
     * @return true if started correctly or false otherwise
     */
    private boolean doWorkerStart(final MaestroNote note, final Class<?> workerClass) {
        if (container.isTestInProgress()) {
            logger.warn("Trying to start a new test, but a test execution is already in progress");
            getClient().notifyFailure("Test already in progress");

            return false;
        }

        final File testLogDir = TestLogUtils.nextTestLogDir(logDir);

        try {
            setupLatencyEvaluator();

            super.writeTestProperties(testLogDir);
            super.writeSystemProperties(testLogDir);

            final TestWorkerInitializer testWorkerInitializer = new TestWorkerInitializer(workerClass, getWorkerOptions());

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

                if (latencyEvaluator != null) {
                    logger.debug("Setting up the observer: worker latency condition");
                    WorkerEvaluatorChecker evaluatorChecker = new WorkerEvaluatorChecker(workers, latencyEvaluator);
                    container.getObservers().add(new EvaluatorObserver(evaluatorChecker));
                }
            }

            logger.debug("Setting up the observer: worker rate writer");
            WorkerRateWriter workerRateWriter = new WorkerRateWriter(testLogDir, workers);
            container.getObservers().add(new RateWriterObserver(workerRateWriter));

            logger.debug("Setting up the observer: worker stale check");
            WorkerStaleChecker workerStaleChecker = new WorkerStaleChecker(workers);
            container.getObservers().add(new StaleObserver(workerStaleChecker));

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

            container.start();

            getClient().replyOk(note);
            return true;
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            getClient().replyInternalError(note, "Unable to start workers from the container: %s", e.getMessage());
        }

        return false;
    }

    private WorkerLatencyWriter getLatencyWriter(File testLogDir, List<MaestroWorker> workers) {

        if (latencyEvaluator == null) {
            return new WorkerLatencyWriter(testLogDir, workers);
        }
        else {
            long reportingInterval = config.getLong("worker.reporting.interval", 10000);
            return new WorkerLatencyWriter(testLogDir, workers, latencyEvaluator,
                    reportingInterval);
        }
    }


    private void setupLatencyEvaluator() {
        long givenLatency = super.getWorkerOptions().getFclAsLong();
        if (givenLatency <= 0) {
            this.latencyEvaluator = null;
            return;
        }

        String policy = config.getString("worker.fcl.default.policy", "soft");

        // The latency comes as milliseconds from the front-end. Therefore, convert them ...
        long maxAcceptableLatency = TimeUnit.MILLISECONDS.toMicros(givenLatency);
        if (policy.equals("soft")) {
            double defaultPercentile = config.getDouble("worker.fcl.soft.percentile", 90.0);

            logger.debug("Setting max latency to {} ms if at percentile {}", givenLatency, defaultPercentile);


            this.latencyEvaluator = new SoftLatencyEvaluator(maxAcceptableLatency, defaultPercentile);
        }
        else {
            logger.debug("Setting max latency to {}", givenLatency);

            this.latencyEvaluator = new HardLatencyEvaluator(maxAcceptableLatency);
        }
    }

    @Override
    public void handle(SetRequest note) {
        super.handle(note);

        getClient().replyOk(note);
    }

    @Override
    public void handle(Halt note) {
        logger.debug("Halt request received");

        container.stop();
        setRunning(false);
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
        statsResponse.setPeerInfo(getPeerInfo());

        LatencyStats latencyStats = container.latencyStats(latencyEvaluator);
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

    private boolean drainStart(final WorkerOptions drainOptions, final MaestroNote note, final Class<MaestroWorker> workerClass) {
        if (container.isTestInProgress()) {
            logger.warn("Trying to start a new drain operation, but a test execution is in progress");
            getClient().notifyFailure("Cannot drain while running a test");

            return false;
        }

        try {
            final TestWorkerInitializer testWorkerInitializer = new TestWorkerInitializer(workerClass, drainOptions);

            int count = getWorkerOptions().getParallelCountAsInt();
            logger.debug("Creating {} workers of type {}", count, workerClass);
            final List<MaestroWorker> workers = container.create(testWorkerInitializer, count);

            if (workers.isEmpty()) {
                logger.warn("No workers were created");

                return false;
            }

            logger.debug("Removing previous observers");
            container.getObservers().clear();

            // Note: it uses the base log dir because of the symlinks
            logger.debug("Setting up the observer: worker shutdown observer");
            WorkerShutdownObserver workerShutdownObserver = new WorkerShutdownObserver(logDir, getClient());
            container.getObservers().add(workerShutdownObserver);

            logger.debug("Setting up the observer: worker cleanup observer");
            container.getObservers().add(new CleanupObserver());

            logger.debug("Starting the workers {}", workerClass);

            container.start();

            getClient().replyOk(note);
            return true;
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            getClient().replyInternalError(note,"Unable to start workers from the container: %s", e.getMessage());
        }

        return false;
    }

    @Override
    public void handle(final DrainRequest note) {
        String className = workersMap.get(note.getWorkerName());
        try {
            Class<MaestroWorker> clazz = (Class<MaestroWorker>) Class.forName(className);

            WorkerOptions drainOptions = new WorkerOptions();

            drainOptions.setBrokerURL(note.getUrl());
            drainOptions.setDuration(note.getDuration());
            drainOptions.setParallelCount(note.getParallelCount());

            if (!drainStart(drainOptions, note, clazz)) {
                logger.error("Unable to start draining from the SUT");
            }
        } catch (ClassNotFoundException e) {
            getClient().replyInternalError(note, "Unable to create a drain worker for %s", note.getWorkerName());

        }
    }


    @Override
    public void handle(final StartWorker note) {
        final String workerName = note.getOptions().getWorkerName();
        logger.info("Start worker {} request received", workerName);

        if (getPeerInfo().getRole() != Role.SENDER && getPeerInfo().getRole() != Role.RECEIVER) {
            getClient().replyInternalError(note, "The worker does not have a role. Set one with RoleAssign");

            return;
        }

        String className = workersMap.get(workerName);
        try {
            Class<?> clazz = (Class) Class.forName(className);

            if (!doWorkerStart(note, clazz)) {
                logger.warn("::handle {} can't start worker", note);
            }
        } catch (ClassNotFoundException e) {
            getClient().replyInternalError(note, "Unable to create an worker for %s", workerName);

        }
    }

    @Override
    public void handle(final StopWorker note) {
        logger.info("Stop worker request received");

        getClient().replyOk(note);

        container.stop();
    }
}
