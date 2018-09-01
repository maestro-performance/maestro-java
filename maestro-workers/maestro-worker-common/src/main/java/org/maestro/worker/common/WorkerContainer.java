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
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.evaluators.Evaluator;
import org.maestro.common.evaluators.LatencyEvaluator;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a container class for multiple workerRuntimeInfos. It is responsible for
 * creating, starting and stopping multiple workerRuntimeInfos at once.
 */
public final class WorkerContainer {
    private static final Logger logger = LoggerFactory.getLogger(WorkerContainer.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final List<WorkerRuntimeInfo> workerRuntimeInfos = new ArrayList<>();
    private final List<WatchdogObserver> observers = new LinkedList<>();
    private static final long TIMEOUT_STOP_WORKER_MILLIS;

    private ExecutorService workerExecutorService;
    private ExecutorService watchdogExecutorService;

    private LocalDateTime startTime;
    private CountDownLatch startSignal;
    private CountDownLatch endSignal;

    static {
        TIMEOUT_STOP_WORKER_MILLIS = config.getLong("maestro.worker.stop.timeout", 1000);
    }


    public WorkerContainer() {
    }


    public List<MaestroWorker> create(final WorkerInitializer initializer, int count) throws IllegalAccessException, InstantiationException {
        workerRuntimeInfos.clear();

        List<MaestroWorker> workers = new ArrayList<>(count);

        if (count > Runtime.getRuntime().availableProcessors()) {
            logger.warn("Trying the create {} worker threads but there is only {} processors available. This can " +
                    "test instability and too many variations on the load generations", count,
                    Runtime.getRuntime().availableProcessors());
        }

        workerExecutorService = Executors.newFixedThreadPool(count, new ThreadFactory() {
            AtomicInteger count = new AtomicInteger();

            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, String.format("worker-%d", count.incrementAndGet()));
            }
        });

        startSignal = new CountDownLatch(count);
        endSignal = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            final MaestroWorker worker = initializer.initialize(i, startSignal, endSignal);

            workers.add(worker);
            WorkerRuntimeInfo ri = new WorkerRuntimeInfo();

            ri.worker = worker;
            workerRuntimeInfos.add(ri);
        }

        watchdogExecutorService = Executors.newSingleThreadScheduledExecutor();

        return workers;
    }

    /**
     * Start the execution of the workers for a predefined class
     */
    public void start() {
        try {
            for (WorkerRuntimeInfo workerRuntimeInfo : workerRuntimeInfos) {
                workerExecutorService.submit(workerRuntimeInfo.worker);
            }

            startSignal.await(10, TimeUnit.SECONDS);

            final WorkerWatchdog workerWatchdog = new WorkerWatchdog(this, workerRuntimeInfos, endSignal);

            watchdogExecutorService.submit(workerWatchdog);

            startTime = LocalDateTime.now();
        }
        catch (Throwable t) {
            workerRuntimeInfos.clear();

            try {
                throw t;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static long getDeadLine(int runningCount) {
        long deadLineAmount = runningCount * TIMEOUT_STOP_WORKER_MILLIS * 2;
        long deadLineMax = config.getLong("worker.active.deadline.max", 65000);

        if (deadLineAmount > deadLineMax) {
            deadLineAmount = deadLineMax;
        }

        return deadLineAmount;
    }

    public void stop() {
        for (WorkerRuntimeInfo ri : workerRuntimeInfos) {
            ri.worker.stop();
        }

        startTime = null;

        if (workerExecutorService != null) {
            try {
                workerExecutorService.awaitTermination(getDeadLine(workerRuntimeInfos.size()), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted ... forcing workers shutdown");
                workerExecutorService.shutdownNow();
            }
        }

        if (watchdogExecutorService != null) {
            try {
                watchdogExecutorService.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted ... forcing watchdog shutdown");
                watchdogExecutorService.shutdownNow();
            }
        }
    }

    /**
     * Checks whether the test is in progress or not
     * @return true if a test is in progress or false otherwise
     */
    public boolean isTestInProgress() {
        if (workerRuntimeInfos.isEmpty()) {
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
        if (workerRuntimeInfos.isEmpty()) {
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
    public LatencyStats latencyStats(final Evaluator<?> evaluator) {
        if (workerRuntimeInfos.isEmpty()) {
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
            endSignal.await(timeout, TimeUnit.SECONDS);
            stop();

            return true;
        } catch (InterruptedException e) {
            logger.debug("Interrupted while waiting for the watchdog to complete running");

            return false;
        }
    }
}
