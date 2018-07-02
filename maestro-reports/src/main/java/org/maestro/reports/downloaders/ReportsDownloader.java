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

import org.maestro.reports.ReportResolver;
import org.maestro.reports.organizer.Organizer;

import java.time.Instant;


/**
 * A reports downloader that collects the reports after the test is complete or on demand via CLI
 */
public interface ReportsDownloader {
    Organizer getOrganizer();

    /**
     * Add a new report resolver with the given host type
     * @param hostType the host type
     * @param reportResolver the report resolver to use for the host type
     */
    void addReportResolver(final String hostType, final ReportResolver reportResolver);
    void downloadLastSuccessful(final String type, final String host);
    void downloadLastFailed(final String type, final String host);
    void downloadAny(final String type, final String host, final String testNumber);

    void waitForComplete();
}
