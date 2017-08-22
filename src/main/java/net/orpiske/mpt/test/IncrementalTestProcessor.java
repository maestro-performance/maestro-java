/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.orpiske.mpt.test;

import net.orpiske.mpt.maestro.client.MaestroNoteProcessor;
import net.orpiske.mpt.maestro.notes.PingResponse;
import net.orpiske.mpt.maestro.notes.TestFailedNotification;
import net.orpiske.mpt.maestro.notes.TestSuccessfulNotification;
import net.orpiske.mpt.reports.ReportsDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IncrementalTestProcessor extends MaestroNoteProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestProcessor.class);

    private ReportsDownloader reportsDownloader;
    private IncrementalTestProfile testProfile;

    private boolean failed = false;
    private int notifications = 0;

    IncrementalTestProcessor(IncrementalTestProfile testProfile, ReportsDownloader reportsDownloader) {
        this.testProfile = testProfile;
        this.reportsDownloader = reportsDownloader;
    }

    @Override
    protected void processPingResponse(PingResponse note) {
        logger.info("Elapsed time from {}: {} ms", note.getName(), note.getElapsed());
    }

    @Override
    protected void processNotifySuccess(TestSuccessfulNotification note) {
        logger.info("Test successful on {} after {} executions", note.getName(),
                testProfile.getTestExecutionNumber());
        logger.info("Test parameters used: " + testProfile.toString());

        String type = note.getName().split("@")[0];
        String host = note.getName().split("@")[1];

        reportsDownloader.setReportTypeDir("success");
        reportsDownloader.downloadLastSuccessful(type, host, note.getName());

        notifications++;
    }

    @Override
    protected void processNotifyFail(TestFailedNotification note) {
        logger.info("Test failed on {} after {} executions", note.getName(),
                testProfile.getTestExecutionNumber());
        logger.info("Test parameter used");

        String type = note.getName().split("@")[0];
        String host = note.getName().split("@")[1];

        reportsDownloader.setReportTypeDir("failed");
        reportsDownloader.downloadLastFailed(type, host, note.getName());

        failed = true;
        notifications++;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isSuccessful() {
        if (testProfile.getParallelCount() >= testProfile.getCeilingParallelCount()) {
            if (testProfile.getRate() >= testProfile.getCeilingRate()) {
                return true;
            }
        }

        return false;
    }

    public boolean isCompleted() {
        if (isFailed()) {
            return true;
        }

        if (isSuccessful()) {
            return true;
        }

        return false;
    }

    public int getNotifications() {
        return notifications;
    }

    public void resetNotifications() {
        this.notifications = 0;
    }
}

