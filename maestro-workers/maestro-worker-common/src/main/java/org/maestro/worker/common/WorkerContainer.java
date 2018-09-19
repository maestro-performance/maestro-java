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
import java.util.Collections;
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

    private final List<MaestroWorker> workers = new LinkedList<>();
    private final List<WatchdogObserver> observers = new LinkedList<>();
    private static final long TIMEOUT_STOP_WORKER_MILLIS;

    private ExecutorService workerExecutorService;
    private ExecutorService watchdogExecutorService;

    private LocalDateTime startTime;
    private CountDownLatch startSignal;
    private CountDownLatch endSignal;

    static {
        TIMEOUT_STOP_WORKER_MILLIS = config.getLong("worker.stop.timeout", 1000);
    }


    /**
     * Create the worker list
     * @param initializer the test worker initializer
     * @param count how many workers to create
     * @return A list of workers
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public List<MaestroWorker> create(final WorkerInitializer initializer, int count) throws IllegalAccessException, InstantiationException {
        workers.clear();

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
        }

        watchdogExecutorService = Executors.newSingleThreadScheduledExecutor();

        return Collections.unmodifiableList(workers);
    }

    /**
     * Start the execution of the workers for a predefined class
     */
    public void start() {
        try {
            workers.forEach(w -> workerExecutorService.submit(w));

            startSignal.await(10, TimeUnit.SECONDS);

            final WorkerWatchdog workerWatchdog = new WorkerWatchdog(this, workers, endSignal);

            watchdogExecutorService.submit(workerWatchdog);

            startTime = LocalDateTime.now();
        }
        catch (Throwable t) {
            workers.clear();

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

    /**
     * Stops the workers on the container
     */
    private void stop(long watchDogTimeout) {
        workers.forEach(w -> w.stop());

        startTime = null;

        if (workerExecutorService != null) {
            try {
                workerExecutorService.shutdown();
                workerExecutorService.awaitTermination(getDeadLine(workers.size()), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted ... forcing workers shutdown");
                workerExecutorService.shutdownNow();
            } finally {
                workerExecutorService = null;
            }
        }

        if (watchdogExecutorService != null) {
            try {
                watchdogExecutorService.shutdown();
                watchdogExecutorService.awaitTermination(watchDogTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted ... forcing watchdog shutdown");
                watchdogExecutorService.shutdownNow();
            } finally {
                watchdogExecutorService = null;
            }
        }
    }

    /**
     * Stops the workers on the container
     */
    public void stop() {
        stop(60);
    }

    /**
     * Checks whether the test is in progress or not
     * @return true if a test is in progress or false otherwise
     */
    public boolean isTestInProgress() {
        if (workers.isEmpty()) {
            return false;
        }

        for (MaestroWorker worker : workers) {
            // A worker should only be in "not running" state if it is being
            // shutdown
            if (!worker.isRunning()) {
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
        if (!isTestInProgress()) {
            return null;
        }

        ThroughputStats ret = new ThroughputStats();

        long messageCount = 0;
        for (MaestroWorker worker : workers) {
            messageCount += worker.messageCount();
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
        if (!isTestInProgress()) {
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

    /**
     * Gets the observers setup of the workers
     * @return a list of observers
     */
    public List<WatchdogObserver> getObservers() {
        return observers;
    }


    /**
     * Waits until the work is complete
     * @param timeout how much to wait before timing out
     * @return false if interrupted or true otherwise
     */
    public boolean waitForComplete(long timeout) {
        stop(timeout);
        return true;
    }
}
