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
 *
 */

package org.maestro.reports.server.collector;

import org.maestro.client.MaestroReceiverClient;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.ErrorUtils;
import org.maestro.common.ResultStrings;
import org.maestro.common.client.notes.ErrorCode;
import org.maestro.common.client.notes.LocationType;
import org.maestro.common.client.notes.Test;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.reports.common.organizer.DefaultOrganizer;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dto.Report;
import org.maestro.reports.server.collector.exceptions.DownloadCountOverflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.maestro.reports.server.collector.LogResponseUtils.save;
import static org.maestro.reports.server.collector.TestDirectoryUtils.getTestDirectory;
import static org.maestro.reports.server.collector.TestDirectoryUtils.getTestIterationDirectory;

public class ReportCollectorWorker {
    private static final Logger logger = LoggerFactory.getLogger(ReportCollectorWorker.class);

    private final Map<PeerInfo, DownloadProgress> aggregatablesMap = new HashMap<>();
    private final ReportDao reportDao = new ReportDao();
    private final File dataDir;
    private final ExecutorService executorService;
    private final MaestroReceiverClient client;

    private DefaultOrganizer organizer;
    private Report report;

    public ReportCollectorWorker(final File dataDir, final MaestroReceiverClient client, final Test test) {
        this.dataDir = dataDir;
        this.client = client;

        executorService = Executors.newSingleThreadExecutor();

        initializeTest(test);
    }

    private int countRemaining() {
        int remaining = 0;
        for (DownloadProgress p : aggregatablesMap.values()) {
            if (p != null) {
                remaining += p.remaining();
            }
        }

        return remaining;
    }

    public boolean isCompleted() {
        int remaining = countRemaining();

        if (remaining > 0) {
            logger.debug("A total of {} files still have files to be downloaded", remaining);
            return false;
        }

        for (DownloadProgress p : aggregatablesMap.values()) {
            if (p == null) {
                return false;
            }
        }

        return true;
    }

    private void logRequest(final MaestroNotification note, LocationType locationType) {
        logger.trace("Sending the log request to {}", this.toString());
        LogRequest request = new LogRequest();

        final String topic = MaestroTopics.peerTopic(note.getId());

        logger.debug("Sending log request to {}", topic);

        request.setLocationType(locationType);
        request.correlate(note);

        try {
            client.publish(topic, request);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response {}", e.getMessage(), e);
        }
    }

    private void createNewReportRecord(final String testResultString, final String message, final String id,
                                       final PeerInfo peerInfo) {
        report.setTestResult(testResultString);
        report.setTestResultMessage(message);
        report.setTestHost(peerInfo.peerHost());
        report.setTestHostRole(peerInfo.getRole().toString());

        final String uniquePeerPath = DefaultOrganizer.generateUniquePeerPath(id, peerInfo);

        String destinationDir = organizer.organize(uniquePeerPath);
        report.setLocation(destinationDir);

        logger.debug("Adding test record to the DB: {}", report);
        reportDao.insert(report);
    }

    public void handle(final TestStartedNotification note) {
        if (!aggregatablesMap.keySet().contains(note.getPeerInfo())) {
            aggregatablesMap.put(note.getPeerInfo(), null);
        }
    }

    public void handle(final StartTestRequest note) {
        /*
         It may receive the test started notification before the start test request,
         so only check if any download file remains to be downloaded and disregard
         null download progress on the progress map
         */
        final int remaining = countRemaining();

        if (remaining > 0) {
            client.replyInternalError(note, ErrorCode.TRY_AGAIN,
                    "There are %d files that are still being downloaded", remaining);
            return;
        }

        client.replyOk(note);
    }

    private void initializeTest(final Test requestedTest) {
        logger.info("Initializing a new test");
        report = new Report();

        int testId = getTestId(requestedTest);
        int testNumber = getTestNumber(requestedTest, testId);

        final File testDataDir = getTestDirectory(testId, dataDir);
        final File testIterationDir = getTestIterationDirectory(testNumber, testDataDir);

        logger.info("Collecting log files on {}", testIterationDir);
        report.setTestId(TestLogUtils.testLogDirNum(testDataDir));

        // Beware: on the report, testNumber is the test iteration.
        report.setTestNumber(TestLogUtils.testLogDirNum(testIterationDir));
        organizer = new DefaultOrganizer(testIterationDir.getPath());

        report.setTestName(requestedTest.getTestName());
        report.setTestScript(requestedTest.getScriptName());
        report.setTestDescription(requestedTest.getTestDetails().getTestDescription());
        report.setTestComments(requestedTest.getTestDetails().getTestComments());
        report.setValid(true);
        report.setRetired(false);
        report.setTestDate(Date.from(Instant.now()));
    }

    private int getTestNumber(Test requestedTest, int testId) {
        int testNumber;

        if (requestedTest.getTestIteration() == Test.NEXT) {
            testNumber = reportDao.getNextTestNumber(testId);
        }
        else {
            testNumber = reportDao.getLastTestNumber(testId);
        }

        return testNumber;
    }

    private int getTestId(Test requestedTest) {
        int testId;

        if (requestedTest.getTestNumber() == Test.NEXT) {
           testId = reportDao.getNextTestId();
        }
        else {
            testId = reportDao.getLastTestId();
        }

        return testId;
    }

    protected void runAggregation(int maxTestId, int maxTestNumber, final AggregationService aggregationService) {
        aggregationService.aggregate(maxTestId, maxTestNumber);
    }

    protected void runAggregation(int maxTestId, int maxTestNumber) {
        runAggregation(maxTestId, maxTestNumber, new AggregationService(dataDir.getPath()));
    }


    public void handle(final LogResponse note) {
        final PeerInfo peerInfo = note.getPeerInfo();

        save(note, organizer);

        trackProgress(note, peerInfo);

        if (isCompleted() && !aggregatablesMap.isEmpty()) {
            logger.info("All downloads currently in progress have finished. Aggregating the data now");
            executorService.submit(() -> runAggregation(report.getTestId(), report.getTestNumber()));

            aggregatablesMap.clear();
        }
    }

    private void trackProgress(LogResponse note, PeerInfo peerInfo) {
        DownloadProgress downloadProgress = aggregatablesMap.get(peerInfo);
        if (downloadProgress == null) {
            downloadProgress = new DownloadProgress(note.getLocationTypeInfo().getFileCount());
        }

        try {
            downloadProgress.increment();
        } catch (DownloadCountOverflowException e) {
            logger.warn("All the files seem to have been downloaded already, therefore not increasing counters");
        }

        aggregatablesMap.put(peerInfo, downloadProgress);
    }

    private static boolean isIgnored(final TestFailedNotification testFailedNotification) {
        return ErrorUtils.isIgnored(testFailedNotification.getPeerInfo().prettyName(), testFailedNotification.getMessage());
    }


    public void handle(final TestFailedNotification note) {
        logRequest(note, LocationType.LAST_FAILED);

        if (isIgnored(note)) {
            createNewReportRecord(ResultStrings.SUCCESS, note.getMessage(), note.getId(), note.getPeerInfo());
        }
        else {
            createNewReportRecord(ResultStrings.FAILED, note.getMessage(), note.getId(), note.getPeerInfo());
        }
    }

    public void handle(final TestSuccessfulNotification note) {
        logRequest(note, LocationType.LAST_SUCCESS);

        createNewReportRecord(ResultStrings.SUCCESS, note.getMessage(), note.getId(), note.getPeerInfo());
    }


}
