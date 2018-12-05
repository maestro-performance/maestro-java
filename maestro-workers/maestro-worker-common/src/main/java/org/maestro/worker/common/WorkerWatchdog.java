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
import org.maestro.common.worker.MaestroWorker;
import org.maestro.worker.common.watchdog.WatchdogObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * The watchdog inspects the active workers to check whether they are still active, completed their job
 * or failed
 */
class WorkerWatchdog implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkerWatchdog.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final WorkerContainer workerContainer;
    private final List<MaestroWorker> workers;

    private final CountDownLatch endSignal;

    /**
     * Constructor
     * @param workers A list of workers to inspect
     */
    public WorkerWatchdog(final WorkerContainer workerContainer,
                          final List<MaestroWorker> workers, final CountDownLatch endSignal) {
        this.workerContainer = workerContainer;
        this.workers = workers;
        this.endSignal = endSignal;
    }


    @Override
    public void run() {
        logger.info("Started the worker watchdog");
        try {
            for (WatchdogObserver observer : workerContainer.getObservers()) {
                observer.onStart();
            }

            logger.debug("Waiting for the workers to signal completion of their work units");
            endSignal.await();
            logger.debug("All workers have finished");
        } catch (InterruptedException e) {
            logger.debug("Watchdog was interrupted");
        } finally {
            int numWorkers = workerContainer.getObservers().size();
            logger.info("Number of workers observers to run: {}", numWorkers);

            try {
                if (numWorkers > 0) {
                    ExecutorService es = Executors.newFixedThreadPool(numWorkers);

                    List<Callable<Boolean>> observers = getObserverCallables(numWorkers);

                    try {
                        es.invokeAll(observers).stream().map(this::checkObserverResult);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while running the observers");
                    }
                }
            }
            finally {
                workers.clear();
            }
        }

        logger.info("Finished running the worker watchdog");
    }

    private List<Callable<Boolean>> getObserverCallables(int numWorkers) {
        List<Callable<Boolean>> observers = new ArrayList<>(numWorkers);

        for (WatchdogObserver observer : workerContainer.getObservers()) {
            observers.add(() -> observer.onStop(workers));
        }
        return observers;
    }


    /**
     * Check the result from the observers
     * @param f observer-returned future
     * @return the value returned by the observer
     */
    private Boolean checkObserverResult(final Future<Boolean> f) {
        Boolean ret = false;
        try {
            long observerDeadline = config.getLong("worker.observer.deadline.secs", 45) + 1;

             ret = f.get(observerDeadline, TimeUnit.SECONDS);

            if (!ret) {
                logger.warn("An observer did not complete successfully");
            }

            return ret;
        } catch (InterruptedException e) {
            logger.warn("Interrupted while running one of the observers");
        } catch (ExecutionException e) {
            logger.error("Failed to execute the observer");
        } catch (TimeoutException e) {
            logger.error("Timed out waiting for the observer to complete");
        }

        return ret;
    }
}
