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

import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.worker.common.MaestroWorkerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DefaultReportsCollector extends MaestroWorkerManager implements MaestroLogCollectorListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultReportsCollector.class);

    private final File dataDir;
    private ReportCollectorWorker reportCollectorWorker;


    public DefaultReportsCollector(final String maestroURL, final PeerInfo peerInfo, final File dataDir) {
        super(maestroURL, peerInfo);

        this.dataDir = dataDir;
        reportCollectorWorker = new ReportCollectorWorker(this.dataDir, getClient());
    }

    @Override
    public void handle(final TestFailedNotification note) {
        super.handle(note);

        reportCollectorWorker.handle(note);
    }

    @Override
    public void handle(final TestSuccessfulNotification note) {
        super.handle(note);

        reportCollectorWorker.handle(note);
    }

    @Override
    public void handle(final LogResponse note) {
        reportCollectorWorker.handle(note);
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

        reportCollectorWorker.handle(note);
    }

    @Override
    public void handle(final StartTestRequest note) {
        reportCollectorWorker.handle(note);
    }
}
