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
import org.maestro.worker.common.watchdog.WatchdogObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The watchdog inspects the active workers to check whether they are still active, completed their job
 * or failed
 */
class WorkerWatchdog implements Runnable {
    public enum WatchdogState {
        STOPPED,
        RUNNING,
        STOPPING
    }

    private static final Logger logger = LoggerFactory.getLogger(WorkerWatchdog.class);

    private final WorkerContainer workerContainer;
    private final List<WorkerRuntimeInfo> workers;

    private volatile WatchdogState watchdogState = WatchdogState.STOPPED;
    private final Evaluator<?> evaluator;


    /**
     * Constructor
     * @param workers A list of workers to inspect
     */
    public WorkerWatchdog(final WorkerContainer workerContainer,
                          final List<WorkerRuntimeInfo> workers,
                          final Evaluator<?> evaluator) {
        this.workerContainer = workerContainer;
        this.workers = new ArrayList<>(workers);

        this.evaluator = evaluator;
    }


    private boolean workersRunning() {
        for (WorkerRuntimeInfo ri : workers) {
            if (!ri.thread.isAlive()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void run() {
        logger.info("Running the worker watchdog");
        watchdogState = WatchdogState.RUNNING;

        try {
            for (WatchdogObserver observer : workerContainer.getObservers()) {
                observer.onStart();
            }

            while (isRunning() && workersRunning()) {
                try {
                    if (evaluator != null) {
                        if (!evaluator.eval()) {

                            /*
                             Note: shot at distance warning. This one will eventually reset
                             the value of the running flag above.

                             TODO: fix this shot-at-distance
                             */

                            workerContainer.fail("The evaluation of the latency condition failed");
                        }
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("The worker thread was interrupted", e);

                    break;
                }
            }
        } finally {
            watchdogState = WatchdogState.STOPPING;
            logger.debug("Waiting for flushing workers's data");

            for (WatchdogObserver observer : workerContainer.getObservers()) {
                if (!observer.onStop(workers)) {
                    logger.debug("Stopping observers because {} returned false", observer.getClass().getName());
                }
            }

            watchdogState = WatchdogState.STOPPED;
        }

        logger.info("Finished running the worker watchdog");
    }

    public boolean isRunning() {
        return watchdogState == WatchdogState.RUNNING;
    }

    public void stop() {
        if (watchdogState == WatchdogState.RUNNING) {
            watchdogState = WatchdogState.STOPPING;
        }
    }
}
