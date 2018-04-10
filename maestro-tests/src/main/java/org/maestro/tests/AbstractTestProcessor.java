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

package org.maestro.tests;

import org.maestro.client.notes.*;
import org.maestro.common.NodeUtils;
import org.maestro.client.exchange.MaestroNoteProcessor;
import org.maestro.common.client.notes.GetOption;
import org.maestro.reports.ReportsDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a generic/reusable test processor that evaluates the individual
 * test responses for every peer on the test cluster and sets the test results
 * accordingly
 */
public abstract class AbstractTestProcessor extends MaestroNoteProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestProcessor.class);
    // Default wait time, in seconds, for the workers to flush their data
    public static final int DEFAULT_WAIT_TIME = 5;

    private final ReportsDownloader reportsDownloader;
    private final AbstractTestProfile testProfile;

    private boolean failed = false;
    private int notifications = 0;

    private int flushWaitSeconds = DEFAULT_WAIT_TIME;
    private Map<String, String> dataServers = new HashMap<>();

    /**
     * Constructor
     * @param testProfile test profile in use
     * @param reportsDownloader A ReportsDownloader instance for downloading the logs and results
     *                          from each of the peers in the test cluster
     */
    public AbstractTestProcessor(AbstractTestProfile testProfile, ReportsDownloader reportsDownloader) {
        this.testProfile = testProfile;
        this.reportsDownloader = reportsDownloader;
    }

    @Override
    protected void processGetResponse(GetResponse note) {
        logger.debug("Processing data server response");
        super.processGetResponse(note);

        if (note.getOption() == GetOption.MAESTRO_NOTE_OPT_GET_DS) {
            logger.info("Registering data server at {}", note.getValue());
            dataServers.put(note.getName(), note.getValue());
        }
    }

    @Override
    protected void processPingResponse(PingResponse note) {
        logger.debug("Elapsed time from {}: {} ms", note.getName(), note.getElapsed());
    }

    protected int getFlushWaitSeconds() {
        return flushWaitSeconds;
    }

    protected void setFlushWaitSeconds(int flushWaitSeconds) {
        this.flushWaitSeconds = flushWaitSeconds;
    }

    // Give some time for the backend to flush their data to disk
    // before downloading
    private void waitForFlush() {
        logger.info("Waiting for {} seconds for the backend to flush their data", flushWaitSeconds);
        try {
            Thread.sleep(flushWaitSeconds * 1000);
        } catch (InterruptedException e) {

        }
    }

    @Override
    protected void processNotifySuccess(TestSuccessfulNotification note) {
        logger.info("Test successful on {} after {} executions", note.getName(),
                testProfile.getTestExecutionNumber());
        logger.info("Test parameters used: " + testProfile.toString());

        String type = NodeUtils.getTypeFromName(note.getName());
        String host = dataServers.get(note.getName());

        // TODO: what if host is null?

        waitForFlush();

        reportsDownloader.getOrganizer().setResultType("success");
        reportsDownloader.downloadLastSuccessful(type, host);

        notifications++;
    }

    @Override
    protected void processNotifyFail(TestFailedNotification note) {
        logger.info("Test failed on {} after {} executions", note.getName(),
                testProfile.getTestExecutionNumber());
        logger.info("Test parameter used");

        String type = NodeUtils.getTypeFromName(note.getName());
        String host = dataServers.get(note.getName());

        // TODO: what if host is null?

        waitForFlush();

        reportsDownloader.getOrganizer().setResultType("failed");
        reportsDownloader.downloadLastFailed(type, host);

        failed = true;
        notifications++;
    }

    /**
     * Tests whether the test was failed
     * @return true if failed or false otherwise
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Tests whether the test was successful
     * @return true if successful or false otherwise
     */
    public abstract boolean isSuccessful();

    public boolean isCompleted() {
        if (isFailed()) {
            return true;
        }

        return isSuccessful();

    }

    /**
     * Returns the number of maestro notifications received
     * @return the number of maestro notifications received
     */
    public int getNotifications() {
        return notifications;
    }

    /**
     * Reset the number of maestro notifications received
     */
    public void resetNotifications() {
        this.notifications = 0;
    }
}
