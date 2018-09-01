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
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.common.worker.WorkerStateInfo;
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

    private final File logDir;
    private final MaestroReceiverClient client;

    public WorkerShutdownObserver(final File logDir, final MaestroReceiverClient client) {
        this.logDir = logDir;
        this.client = client;
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
    public boolean onStop(final List<MaestroWorker> workers) {
        boolean failed = false;
        String exceptionMessage = null;

        try {
            for (MaestroWorker worker : workers) {
                if (worker != null) {
                    WorkerStateInfo wsi = worker.getWorkerState();
                    if (wsi == null) {
                        logger.error("Invalid worker state information");

                    } else {
                        if (!isCleanExit(wsi)) {
                            failed = true;
                            exceptionMessage = Objects.requireNonNull(wsi.getException()).getMessage();

                            break;
                        }
                    }
                }
            }
        }
        finally {
            // if null => drain worker observer
            if (logDir != null) {
                TestLogUtils.createSymlinks(logDir, failed);
            }

            sendTestNotification(failed, exceptionMessage);
        }

        return true;
    }
}
