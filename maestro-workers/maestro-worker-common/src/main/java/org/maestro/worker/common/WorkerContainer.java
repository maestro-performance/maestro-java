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

import org.maestro.common.evaluators.Evaluator;
import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.*;
import org.maestro.worker.common.container.initializers.WorkerInitializer;
import org.maestro.worker.common.watchdog.WatchdogObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a container class for multiple workerRuntimeInfos. It is responsible for
 * creating, starting and stopping multiple workerRuntimeInfos at once.
 */
public final class WorkerContainer {

    private static WorkerContainer instance;
    private final List<WorkerRuntimeInfo> workerRuntimeInfos = new ArrayList<>();
    private final List<WatchdogObserver> observers = new LinkedList<>();

    private WorkerWatchdog workerWatchdog;
    private Thread watchDogThread;
    private LocalDateTime startTime;
    private Evaluator<?> evaluator;

    public WorkerContainer() {
    }


    public List<MaestroWorker> create(final WorkerInitializer initializer, int count) throws IllegalAccessException, InstantiationException {
        workerRuntimeInfos.clear();

        List<MaestroWorker> workers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            final MaestroWorker worker = initializer.initialize(i);

            workers.add(worker);

            WorkerRuntimeInfo ri = new WorkerRuntimeInfo();

            ri.worker = worker;
            ri.thread = new Thread(ri.worker);
            workerRuntimeInfos.add(ri);
        }

        return workers;
    }

    /**
     * Start the execution of the workers for a predefined class
     * @param evaluator The evaluator that is run along w/ the worker watchdog (ie.: to evaluate the FCL/latency)
     * @throws IllegalAccessException if unable to access the worker constructor
     * @throws InstantiationException if unable to instantiate the worker
     */
    public void start(final Evaluator<?> evaluator) {

        this.evaluator = evaluator;

        try {
            for (WorkerRuntimeInfo workerRuntimeInfo : workerRuntimeInfos) {
                workerRuntimeInfo.thread.start();
            }

            workerWatchdog = new WorkerWatchdog(this, workerRuntimeInfos, evaluator);

            watchDogThread = new Thread(workerWatchdog);
            watchDogThread.start();

            startTime = LocalDateTime.now();

        }
        catch (Throwable t) {
            workerRuntimeInfos.clear();

            throw t;
        }
    }

    public void stop() {
        if (workerWatchdog != null) {
            if (workerWatchdog.isRunning()) {
                for (WorkerRuntimeInfo ri : workerRuntimeInfos) {
                    ri.worker.stop();
                }

                workerWatchdog.setRunning(false);
            }

            startTime = null;
        }
    }

    public void fail(final String message) {
        if (workerWatchdog != null) {
            if (workerWatchdog.isRunning()) {
                MaestroException exception = new MaestroException(message);

                for (WorkerRuntimeInfo ri : workerRuntimeInfos) {
                    ri.worker.fail(exception);
                }

                workerWatchdog.setRunning(false);
            }

            startTime = null;
        }
    }

    private boolean watchdogRunning() {
        if (workerWatchdog == null) {
            return false;
        }

        return workerWatchdog.isRunning();
    }

    /**
     * Checks whether the test is in progress or not
     * @return true if a test is in progress or false otherwise
     */
    public boolean isTestInProgress() {
        if (!watchdogRunning()) {
            return false;
        }

        for (WorkerRuntimeInfo ri : workerRuntimeInfos) {
            // A worker should only be in "not running" state if it is being
            // shutdown
            if (!ri.worker.isRunning()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the throughput statistics
     * @return the throughput statistics
     */
    public ThroughputStats throughputStats() {
        if (!watchdogRunning()) {
            return null;
        }

        ThroughputStats ret = new ThroughputStats();

        long messageCount = 0;
        for (WorkerRuntimeInfo runtimeInfo : workerRuntimeInfos) {
            messageCount += runtimeInfo.worker.messageCount();
        }
        ret.setCount(messageCount);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(startTime, now);
        ret.setDuration(duration);

        return ret;
    }



    /**
     * Gets the latency statistics
     * @return the latency statistics or null if not applicable for the work set in the container
     */
    public LatencyStats latencyStats() {
        if (!watchdogRunning()) {
            return null;
        }

        if (evaluator instanceof LatencyEvaluator) {
            LatencyStats ret = new LatencyStats();
            
            LatencyEvaluator latencyEvaluator = (LatencyEvaluator) evaluator;

            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(startTime, now);
            ret.setDuration(duration);

            ret.setLatency(latencyEvaluator.getMean());

            return ret;
        }

        return null;
    }

    public List<WatchdogObserver> getObservers() {
        return observers;
    }

    public boolean waitForComplete(long timeout) {
        try {
            watchDogThread.join(timeout);

            return watchDogThread.isAlive();
        } catch (InterruptedException e) {
            Logger logger = LoggerFactory.getLogger(WorkerContainer.class);

            logger.debug("Interrupted while waiting for the watchdog to complete running");
        }

        return false;
    }
}
