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

package org.maestro.worker.common.watchdog;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.MaestroReceiverClient;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.common.worker.WorkerStateInfo;
import org.maestro.worker.common.WorkerRuntimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.maestro.worker.common.WorkerStateInfoUtil.isCleanExit;


/**
 * An observer for the watchdog that handles the worker shutdown process,
 * its terminal state and symlink creation on the log directory
 */
public class WorkerShutdownObserver implements WatchdogObserver {
    private static final Logger logger = LoggerFactory.getLogger(WorkerShutdownObserver.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private static final long TIMEOUT_STOP_WORKER_MILLIS;

    private final File logDir;
    private final MaestroReceiverClient client;

    static {
        TIMEOUT_STOP_WORKER_MILLIS = config.getLong("maestro.worker.stop.timeout", 1000);
    }

    public WorkerShutdownObserver(final File logDir, final MaestroReceiverClient client) {
        this.logDir = logDir;
        this.client = client;
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

    private void sendTestNotification(boolean failed, String exceptionMessage) {

        if (failed) {
            if (exceptionMessage != null) {
                client.notifyFailure(exceptionMessage);
            }
            else {
                client.notifyFailure("Unhandled worker error");
            }
        }
        else {
            client.notifySuccess("Test completed successfully");
        }
    }

    @Override
    public boolean onStop(final List<WorkerRuntimeInfo> workers) {
        boolean failed = false;
        String exceptionMessage = null;

        try {
            final long startWaitingWorkers = System.currentTimeMillis();
            if (awaitWorkers(startWaitingWorkers, workers) > 0) {
                logger.warn("The writer will be forced to stop with alive workers");
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
        }
        finally {
            TestLogUtils.createSymlinks(logDir, failed);

            sendTestNotification(failed, exceptionMessage);
        }

        return true;
    }
}
