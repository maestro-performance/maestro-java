package org.maestro.tests.callbacks;

import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.notes.TestFailedNotification;
import org.maestro.client.notes.TestSuccessfulNotification;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.tests.rate.FixedRateTestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadCallback implements MaestroNoteCallback {
    private static final Logger logger = LoggerFactory.getLogger(DownloadCallback.class);

    final FixedRateTestExecutor executor;

    public DownloadCallback(FixedRateTestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void call(MaestroNote note) {
        if (!executor.isRunning()) {
            return;
        }

        if (note instanceof TestSuccessfulNotification) {
            executor.getTestProcessor().processNotifySuccess((TestSuccessfulNotification) note);
        }
        else {
            if (note instanceof TestFailedNotification) {
                executor.getTestProcessor().processNotifyFail((TestFailedNotification) note);
            }
        }
    }
}
