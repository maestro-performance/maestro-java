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

import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.ErrorCode;
import org.maestro.common.client.notes.LocationType;
import org.maestro.common.client.notes.Test;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.reports.common.organizer.DefaultOrganizer;
import org.maestro.common.ResultStrings;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dto.Report;
import org.maestro.worker.common.MaestroWorkerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static org.maestro.reports.server.collector.LogResponseUtils.save;

public class DefaultReportsCollector extends MaestroWorkerManager implements MaestroLogCollectorListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultReportsCollector.class);
    private final ReportDao reportDao = new ReportDao();

    private DefaultOrganizer organizer;

    private final File dataDir;
    private Report report;
    private final ExecutorService executorService;

    private final Map<PeerInfo, DownloadProgress> progressMap = new HashMap<>();

    public DefaultReportsCollector(final String maestroURL, final PeerInfo peerInfo, final File dataDir) {
        super(maestroURL, peerInfo);

        this.dataDir = dataDir;
        executorService = Executors.newSingleThreadExecutor();
    }

    private void runAggregation(int maxTestId, int maxTestNumber) {
        final AggregationService aggregationService = new AggregationService(dataDir.getPath());

        aggregationService.aggregate(maxTestId, maxTestNumber);
    }

    protected void logRequest(final MaestroNotification note, LocationType locationType) {
        logger.trace("Sending the log request to {}", this.toString());
        LogRequest request = new LogRequest();

        final String topic = MaestroTopics.peerTopic(note.getId());

        logger.debug("Sending log request to {}", topic);

        request.setLocationType(locationType);
        request.correlate(note);

        try {
            super.getClient().publish(topic, request);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response {}", e.getMessage(), e);
        }
    }

    @Override
    public void handle(final TestFailedNotification note) {
        super.handle(note);

        logRequest(note, LocationType.LAST_FAILED);

        createNewReportRecord(ResultStrings.FAILED, note.getPeerInfo());
    }

    @Override
    public void handle(final TestSuccessfulNotification note) {
        super.handle(note);

        logRequest(note, LocationType.LAST_SUCCESS);

        createNewReportRecord(ResultStrings.SUCCESS, note.getPeerInfo());
    }

    private void createNewReportRecord(final String testResultString, final PeerInfo peerInfo) {
        report.setTestResult(testResultString);
        report.setTestHost(peerInfo.peerHost());
        report.setTestHostRole(peerInfo.getRole().toString());

        String destinationDir = organizer.organize(peerInfo);
        report.setLocation(destinationDir);

        logger.debug("Adding test record to the DB: {}", report);
        reportDao.insert(report);
    }

    private int countRemaining() {
        int remaining = 0;
        for (DownloadProgress p : progressMap.values()) {
            if (p != null) {
                remaining += p.remaining();
            }
        }

        return remaining;
    }

    private boolean isCompleted() {
        int remaining = countRemaining();

        if (remaining > 0) {
            logger.debug("A total of {} files still have files to be downloaded", remaining);
            return false;
        }

        for (DownloadProgress p : progressMap.values()) {
            if (p == null) {
                return false;
            }
        }

        return true;
    }


    @Override
    public void handle(final LogResponse note) {
        final PeerInfo peerInfo = note.getPeerInfo();

        DownloadProgress downloadProgress = progressMap.get(peerInfo);
        if (downloadProgress == null) {
            downloadProgress = new DownloadProgress(note.getLocationTypeInfo().getFileCount());
        }

        downloadProgress.increment();
        progressMap.put(peerInfo, downloadProgress);
        save(note, organizer);

        if (isCompleted() && !progressMap.isEmpty()) {
            logger.info("All downloads currently in progress have finished. Aggregating the data now");
            executorService.submit(() -> runAggregation(report.getTestId(), report.getTestNumber()));

            progressMap.clear();

            report = null;
        }
    }

    @Override
    public void handle(final LogRequest note) {
        // no-op
    }

    @Override
    public void handle(final PingRequest note) throws MaestroConnectionException, MalformedNoteException {

    }

    @Override
    public void handle(final StartWorker note) {

    }

    @Override
    public void handle(final StopWorker note) {

    }

    @Override
    public void handle(final RoleAssign note) {
    }

    @Override
    public void handle(final TestStartedNotification note) {
        super.handle(note);

        if (!progressMap.keySet().contains(note.getPeerInfo())) {
            progressMap.put(note.getPeerInfo(), null);
        }
    }

    @Override
    public void handle(final StartTestRequest note) {


        if (!isCompleted()) {
            final long remaining = countRemaining();

            super.getClient().replyInternalError(note, ErrorCode.TRY_AGAIN,
                     "There are %d files that are still being downloaded", remaining);
            return;
        }

        final Test requestedTest = note.getTest();

        report = new Report();

        final File testDataDir = getTestDirectory(requestedTest, dataDir);
        final File testIterationDir = getTestIterationDirectory(requestedTest, testDataDir);

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

        super.getClient().replyOk(note);
    }

    private static File getTestDirectory(final File baseDir, int num) {
        File testDir;

        if (num == Test.NEXT) {
            testDir = TestLogUtils.nextTestLogDir(baseDir);
        }
        else {
            if (num == Test.LAST) {
                testDir = TestLogUtils.findLastLogDir(baseDir);
            }
            else {
                testDir = TestLogUtils.testLogDir(baseDir, num);
            }
        }

        return testDir;
    }

    private static File getTestDirectory(final Test requestedTest, final File baseDir) {
        int requestedTestNumber = requestedTest.getTestNumber();

        return getTestDirectory(baseDir, requestedTestNumber);
    }

    private static File getTestIterationDirectory(final Test requestedTest, final File baseDir) {
        int requestedTestIteration = requestedTest.getTestIteration();

        return getTestDirectory(baseDir, requestedTestIteration);
    }
}
