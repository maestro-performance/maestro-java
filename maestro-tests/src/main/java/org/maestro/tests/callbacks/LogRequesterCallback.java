/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.tests.callbacks;

import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.notes.TestFailedNotification;
import org.maestro.client.notes.TestSuccessfulNotification;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.DownloadProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A callback that request/downloads log files based on the test notification
 * received.
 */
public class LogRequesterCallback implements MaestroNoteCallback {
    private static final Logger logger = LoggerFactory.getLogger(LogRequesterCallback.class);
    private final AbstractTestExecutor executor;
    private final DownloadProcessor downloadProcessor;

    public LogRequesterCallback(final AbstractTestExecutor executor, final DownloadProcessor downloadProcessor) {
        this.executor = executor;
        this.downloadProcessor = downloadProcessor;
    }

    @Override
    public boolean call(MaestroNote note) {
        if (!executor.isRunning()) {
            return true;
        }

        if (note instanceof TestSuccessfulNotification) {
            downloadProcessor.download((TestSuccessfulNotification) note);
        }
        else {
            if (note instanceof TestFailedNotification) {
                downloadProcessor.download((TestFailedNotification) note);
            }
        }

        return true;
    }
}
