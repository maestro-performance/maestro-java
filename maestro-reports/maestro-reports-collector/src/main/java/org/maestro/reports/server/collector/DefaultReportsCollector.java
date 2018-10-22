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
    private final AggregationService aggregationService;

    private final Map<PeerInfo, DownloadProgress> progressMap = new HashMap<>();
    private final Set<PeerInfo> knownPeers = new TreeSet<>();

    public DefaultReportsCollector(final String maestroURL, final PeerInfo peerInfo, final File dataDir) {
        super(maestroURL, peerInfo);

        this.dataDir = dataDir;
        aggregationService = new AggregationService(dataDir.getPath());
    }

    private void runAggregation() {
        aggregationService.aggregate();
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

        logger.info("Adding test record to the DB");
        reportDao.insert(report);
    }

    private boolean allNodesDownloaded() {
        final long progress = progressMap.keySet().size();
        final long knownNodeCount = knownPeers.size();

        if (progressMap.size() == 0) {
            return false;
        }

        logger.debug("Checking completion status: {} nodes still have not provided any files",
                (knownNodeCount - progress));
        if (progress >= knownNodeCount) {
            return true;
        }

        return false;
    }

    private long countInProgress() {
        return progressMap.values().stream().filter(DownloadProgress::inProgress).count();
    }

    private boolean isCompleted() {
        if (!allNodesDownloaded()) {
            return false;
        }

        final long inProgress = countInProgress();

        if (inProgress == 0) {
            logger.debug("All downloads seem to have been completed");
            return true;
        }
        else {
            logger.debug("A total of {} nodes still have files to be downloaded", inProgress);
            return false;
        }
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

        if (isCompleted()) {
            logger.info("All downloads currently in progress have finished. Aggregating the data now");
            Executors.newSingleThreadExecutor().submit(this::runAggregation);

            progressMap.clear();
            knownPeers.clear();

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

        knownPeers.add(note.getPeerInfo());
    }

    @Override
    public void handle(final StartTestRequest note) {
        long inProgress = countInProgress();
        if (inProgress > 0) {
            logger.warn("There are {} peers with files still being downloaded. Requesting the client to wait",
                    inProgress);

             super.getClient().replyInternalError(note, ErrorCode.TRY_AGAIN,
                     "There are %d peers with files still being downloaded", inProgress);
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
