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
