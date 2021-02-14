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

import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.Test;
import org.maestro.common.client.notes.TestExecutionInfo;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.worker.common.MaestroWorkerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DefaultReportsCollector extends MaestroWorkerManager implements MaestroLogCollectorListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultReportsCollector.class);

    private final File dataDir;
    private final Map<Test, ReportCollectorWorker> workerMap = new HashMap<>();
    private final Map<String, Test> testMap = new HashMap<>();
    private final ReportCollectorWorkerFactory reportCollectorWorkerFactory;

    public DefaultReportsCollector(MaestroClient maestroClient, ConsumerEndpoint consumerEndpoint, PeerInfo peerInfo, final File dataDir) {
        this(maestroClient, consumerEndpoint, peerInfo, dataDir, new DefaultReportCollectorWorkerFactory());
    }

    protected DefaultReportsCollector(MaestroClient maestroClient, ConsumerEndpoint consumerEndpoint, PeerInfo peerInfo, final File dataDir,
                                      final ReportCollectorWorkerFactory reportCollectorWorkerFactory) {
        super(maestroClient, consumerEndpoint, peerInfo);

        this.dataDir = dataDir;
        this.reportCollectorWorkerFactory = reportCollectorWorkerFactory;
    }

    protected ReportCollectorWorker getCollectorWorker(final Test test) {
        return workerMap.get(test);
    }


    protected ReportCollectorWorker getCollectorWorker(final TestExecutionInfo testExecutionInfo) {
        ReportCollectorWorker reportCollectorWorker = workerMap.get(testExecutionInfo.getTest());
        if (reportCollectorWorker == null) {
            reportCollectorWorker = reportCollectorWorkerFactory.newWorker(this.dataDir, getClient(), testExecutionInfo);
        }

        workerMap.put(testExecutionInfo.getTest(), reportCollectorWorker);
        return reportCollectorWorker;
    }

    @Override
    public void handle(final TestFailedNotification note) {
        super.handle(note);

        ReportCollectorWorker reportCollectorWorker = getCollectorWorker(note.getTest());
        if (reportCollectorWorker == null) {
            logger.warn("It is possible that the server was launched while a test execution was in progress. " +
                    "Ignoring the current note");

            return;
        }

        reportCollectorWorker.handle(note);

        final String id = note.getId();
        final Test test = note.getTest();

        logger.info("Associating ID {} with test {}", id, test);
        testMap.put(id, test);
    }

    @Override
    public void handle(final TestSuccessfulNotification note) {
        super.handle(note);

        ReportCollectorWorker reportCollectorWorker = getCollectorWorker(note.getTest());

        if (reportCollectorWorker == null) {
            logger.warn("It is possible that the server was launched while a test execution was in progress. " +
                    "Ignoring the current note");

            return;
        }

        reportCollectorWorker.handle(note);

        final String id = note.getId();
        final Test test = note.getTest();

        logger.info("Associating ID {} with test {}", id, test);
        testMap.put(id, test);
    }

    @Override
    public void handle(final LogResponse note) {
        Test test = testMap.get(note.getId());

        if (test == null) {
            logger.error("There is not test object associated with a message with ID {}", note.getId());
            logger.error("Skipping file {} from {}", note.getFileName(), note.getPeerInfo().prettyName());

            return;
        }

        ReportCollectorWorker reportCollectorWorker = getCollectorWorker(test);
        if (reportCollectorWorker == null) {
            logger.warn("It is possible that the server was launched while a test execution was in progress. " +
                    "Ignoring the current note");

            return;
        }

        reportCollectorWorker.handle(note);

        if (reportCollectorWorker.isCompleted()) {
            logger.info("Test transaction is complete, removing the objects from the caches");
            workerMap.remove(test);
            testMap.remove(note.getId());
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
    public void handle(RoleUnassign note) {

    }

    @Override
    public void handle(final TestStartedNotification note) {
        super.handle(note);

        ReportCollectorWorker reportCollectorWorker = getCollectorWorker(note.getTest());
        reportCollectorWorker.handle(note);
    }

    @Override
    public void handle(final StartTestRequest note) {
        logger.debug("Test started request received");

        ReportCollectorWorker reportCollectorWorker = getCollectorWorker(note.getTestExecutionInfo());

        reportCollectorWorker.handle(note);

    }
}
