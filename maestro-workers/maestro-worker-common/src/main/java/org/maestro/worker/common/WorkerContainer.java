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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This is a container class for multiple workerRuntimeInfos. It is responsible for
 * creating, starting and stopping multiple workerRuntimeInfos at once.
 */
public final class WorkerContainer {
    private static WorkerContainer instance;
    private WorkerOptions workerOptions;
    private final List<WorkerRuntimeInfo> workerRuntimeInfos = new ArrayList<>();

    private WorkerWatchdog workerWatchdog;
    private Thread watchDogThread;
    private LocalDateTime startTime;
    private Evaluator<?> evaluator;

    private WorkerContainer() {
    }

    /**
     * Gets and instance of the container
     * @return the instance of the container
     */
    public synchronized static WorkerContainer getInstance() {
        if (instance == null) {
            instance = new WorkerContainer();
        }

        return instance;
    }


    /**
     * Sets the worker options for the instance.
     * @param workerOptions the worker options to set
     */
    public void setWorkerOptions(WorkerOptions workerOptions) {
        this.workerOptions = workerOptions;
    }

    /**
     * Start the execution of the workers for a predefined class
     * @param clazz The class associated with the workers
     * @param workers The collection to contain the workers
     * @param onWorkersStopped callback that will be called when the workers will stop
     * @param evaluator The evaluator that is run along w/ the worker watchdog (ie.: to evaluate the FCL/latency)
     * @throws IllegalAccessException if unable to access the worker constructor
     * @throws InstantiationException if unable to instantiate the worker
     */
    public void start(final Class<MaestroWorker> clazz, Collection<? super MaestroWorker> workers,
                      Consumer<? super List<WorkerRuntimeInfo>> onWorkersStopped, Evaluator<?> evaluator)
            throws IllegalAccessException, InstantiationException {
        final int parallelCount = Integer.parseInt(workerOptions.getParallelCount());
        this.workerRuntimeInfos.clear();
        this.evaluator = evaluator;
        try {
            createAndStartWorkers(clazz, workerOptions, parallelCount, this.workerRuntimeInfos, onWorkersStopped, evaluator);
        } catch (Throwable t) {
            //interrupt any workers
            this.workerRuntimeInfos.forEach(info -> info.thread.interrupt());
            //cleanup
            this.workerRuntimeInfos.clear();
            throw t;
        }
        //the workers are started
        workers.addAll(this.workerRuntimeInfos.stream().map(info -> info.worker).collect(Collectors.toList()));
        startTime = LocalDateTime.now();
    }

    private void createAndStartWorkers(final Class<MaestroWorker> clazz, WorkerOptions workerOptions, int workers,
                                       List<WorkerRuntimeInfo> workerRuntimeInfos,
                                       final Consumer<? super List<WorkerRuntimeInfo>> onWorkersStopped,
                                       final Evaluator<?> evaluator) throws IllegalAccessException, InstantiationException {
        for (int i = 0; i < workers; i++) {
            final WorkerRuntimeInfo ri = new WorkerRuntimeInfo();
            ri.worker = clazz.newInstance();
            ri.worker.setWorkerOptions(workerOptions);
            ri.worker.setWorkerNumber(i);
            ri.thread = new Thread(ri.worker);
            ri.thread.start();
            workerRuntimeInfos.add(ri);
        }

        workerWatchdog = new WorkerWatchdog(workerRuntimeInfos, onWorkersStopped, evaluator);

        watchDogThread = new Thread(workerWatchdog);
        watchDogThread.start();
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
}
