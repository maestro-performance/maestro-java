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

import org.maestro.client.MaestroReceiverClient;
import org.maestro.common.client.notes.Test;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.common.worker.WorkerStateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.maestro.worker.common.WorkerStateInfoUtil.isCleanExit;


/**
 * An observer for the watchdog that handles the worker shutdown process,
 * its terminal state and symlink creation on the log directory
 */
public class WorkerShutdownObserver implements WatchdogObserver {
    private static final Logger logger = LoggerFactory.getLogger(WorkerShutdownObserver.class);

    private final File logDir;
    private final MaestroReceiverClient client;
    private final Test test;

    public WorkerShutdownObserver(final File logDir, final MaestroReceiverClient client, final Test test) {
        this.logDir = logDir;
        this.client = client;
        this.test = test;
    }

    private void sendTestNotification(boolean failed, final Exception exception) {
        if (failed) {
            String exceptionMessage;
            if (exception == null) {
                logger.warn("Worker failed but no exception was provided: it is likely that the worker shutdown " +
                        "time out expired before the client library finalized its work or that it was hang");
                exceptionMessage = "Worker failed but no exception was provided: it is likely that the worker shutdown"
                        + " time out expired before the client library finalized its work or that it was hang";
            }
            else {
                exceptionMessage = exception.getMessage();
                if (exceptionMessage == null) {
                    logger.warn("The worked supposedly with {}, but no message was provided", exception.getClass(),
                            exception);
                    exceptionMessage = String.format("Worker failed with %s but no exception was provided",
                            exception.getClass());
                }
            }

            if (exceptionMessage != null) {
                client.notifyFailure(test, exceptionMessage);
            }
        }
        else {
            client.notifySuccess(test, "Test completed successfully");
        }
    }

    @Override
    public boolean onStop(final List<MaestroWorker> workers) {
        boolean failed = false;
        Exception exception = null;

        try {
            for (MaestroWorker worker : workers) {
                if (worker != null) {
                    WorkerStateInfo wsi = worker.getWorkerState();
                    if (wsi == null) {
                        logger.error("Invalid worker state information");

                    } else {
                        if (!isCleanExit(wsi)) {
                            failed = true;
                            exception = wsi.getException();

                            break;
                        }
                    }
                }
            }
        }
        finally {
            if (logDir != null) {
                TestLogUtils.createSymlinks(logDir, failed);
            }

            sendTestNotification(failed, exception);
        }

        return true;
    }
}
