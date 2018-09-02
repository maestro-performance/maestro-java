/*
 * Copyright 2018 Otavio Rodolfo Piske
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

import org.maestro.client.notes.GetResponse;
import org.maestro.client.notes.TestFailedNotification;
import org.maestro.client.notes.TestSuccessfulNotification;
import org.maestro.common.client.notes.GetOption;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Download the test reports
 */
public class DownloadProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DownloadProcessor.class);
    private final Map<String, String> dataServers = new HashMap<>();
    private final ReportsDownloader reportsDownloader;

    public DownloadProcessor(final ReportsDownloader reportsDownloader) {
        this.reportsDownloader = reportsDownloader;
    }

    /**
     * Download the reports for test successful notifications
     * @param note the success notification
     * @return true if downloaded successfully or false otherwise
     */
    public boolean download(final TestSuccessfulNotification note) {
        logger.debug("Downloading {}", note);
        reportsDownloader.downloadLastSuccessful(note.getId(), note.getPeerInfo());
        return true;
    }


    /**
     * Download the reports for test failed notifications
     * @param note the failed notification
     * @return true if downloaded successfully or false otherwise
     */
    public boolean download(final TestFailedNotification note) {
        logger.debug("Downloading {}", note);
        reportsDownloader.downloadLastFailed(note.getId(), note.getPeerInfo());

        return true;
    }


    /**
     * Register a data server
     * @param note the get response note containing the data server address
     */
    public void addDataServer(final GetResponse note) {
        if (note.getOption() == GetOption.MAESTRO_NOTE_OPT_GET_DS) {
            logger.info("Registering data server at {}", note.getValue());
            dataServers.put(note.getPeerInfo().prettyName(), note.getValue());
        }
    }
}
