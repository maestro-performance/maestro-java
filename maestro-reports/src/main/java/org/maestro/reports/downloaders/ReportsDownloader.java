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

package org.maestro.reports.downloaders;

import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.reports.organizer.Organizer;

/**
 * A reports downloader that collects the reports after the test is complete or on demand via CLI
 */
public interface ReportsDownloader {
    Organizer getOrganizer();

    /**
     * Download the last successful reports
     * @param id peer id
     * @param peerInfo the peer information
     */
    void downloadLastSuccessful(final String id, final PeerInfo peerInfo);

    /**
     * Download the last failed reports
     * @param id peer id
     * @param peerInfo the peer information
     */
    void downloadLastFailed(final String id, final PeerInfo peerInfo);

    /**
     * Download any report
     * @param id peer id
     * @param testNumber test number to download the reports
     */
    void downloadAny(final String id, final String testNumber);


    /**
     * Wait for the downloads to complete
     */
    void waitForComplete();
}
