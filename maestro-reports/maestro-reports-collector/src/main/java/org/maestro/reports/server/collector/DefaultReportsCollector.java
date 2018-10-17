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
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.maestro.reports.server.collector.LogResponseUtils.save;

public class DefaultReportsCollector extends MaestroWorkerManager implements MaestroLogCollectorListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultReportsCollector.class);
    private ReportDao reportDao = new ReportDao();

    private DefaultOrganizer organizer;

    private final File dataDir;
    private Report report;
    private Instant lastDownload = Instant.now();
    private AggregationService aggregationService;

    public DefaultReportsCollector(final String maestroURL, final PeerInfo peerInfo, final File dataDir) {
        super(maestroURL, peerInfo);

        this.dataDir = dataDir;
        aggregationService = new AggregationService(dataDir.getPath());

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::runAggregation, 0, 60,
                TimeUnit.SECONDS);
    }

    private void runAggregation() {
        long elapsed = Duration.between(lastDownload, Instant.now()).getSeconds();

        logger.info("Running the aggregation service: {}", elapsed);
        if (elapsed >= 60) {
            aggregationService.aggregate();
        }
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



    @Override
    public void handle(LogResponse note) {
        save(note, organizer);

        lastDownload = Instant.now();
    }

    @Override
    public void handle(LogRequest note) {
        // no-op
    }

    @Override
    public void handle(PingRequest note) throws MaestroConnectionException, MalformedNoteException {

    }

    @Override
    public void handle(StartWorker note) {

    }

    @Override
    public void handle(StopWorker note) {

    }

    @Override
    public void handle(RoleAssign note) {
    }

    @Override
    public void handle(StartTestRequest note) {
        Test requestedTest = note.getTest();

        report = new Report();

        if (requestedTest.getTestNumber() == Test.NEXT) {
            final File testDataDir = TestLogUtils.nextTestLogDir(dataDir);
            final File lastIterationDir = new File(testDataDir, "0");

            logger.info("Collecting log files on {}", lastIterationDir);
            organizer = new DefaultOrganizer(lastIterationDir.getPath());

            report.setTestId(TestLogUtils.testLogDirNum(testDataDir));
            report.setTestNumber(TestLogUtils.testLogDirNum(lastIterationDir));
        }
        else {
            final File lastTestDir = TestLogUtils.findLastLogDir(dataDir);
            final File lastIterationDir = TestLogUtils.findLastLogDir(lastTestDir);

            logger.info("Collecting log files on {}", lastIterationDir);
            organizer = new DefaultOrganizer(lastIterationDir.getPath());

            report.setTestId(TestLogUtils.testLogDirNum(lastTestDir));
            report.setTestNumber(TestLogUtils.testLogDirNum(lastIterationDir));
        }

        report.setTestName(note.getTest().getTestName());
        report.setTestScript(note.getTest().getScriptName());

        super.getClient().replyOk(note);
    }
}
