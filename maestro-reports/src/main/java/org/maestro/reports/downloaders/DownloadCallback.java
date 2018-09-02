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

package org.maestro.reports.downloaders;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.notes.LogResponse;
import org.maestro.common.Monitor;
import org.maestro.common.client.notes.LocationType;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.contrib.utils.digest.Sha1Digest;
import org.maestro.reports.organizer.Organizer;
import org.maestro.reports.organizer.ResultStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Maestro Note callback mechanism responsible for downloading the log files
 */
class DownloadCallback implements MaestroNoteCallback {
    private static final Logger logger = LoggerFactory.getLogger(DownloadCallback.class);
    private final Organizer organizer;
    private final Sha1Digest digest = new Sha1Digest();
    private Monitor<Object> monitor;
    private AtomicInteger downloadCount = new AtomicInteger(0);

    DownloadCallback(final Organizer organizer, final Monitor<Object> monitor) {
        this.organizer = organizer;
        this.monitor = monitor;
    }

    private void save(final LogResponse logResponse) {
        switch (logResponse.getLocationType()) {
            case LAST_SUCCESS: {
                organizer.setResultType(ResultStrings.SUCCESS);
                break;
            }
            default: {
                organizer.setResultType(ResultStrings.FAILED);
                break;
            }
        }

        String destDir = organizer.organize(logResponse.getPeerInfo());
        File outFile = new File(destDir, logResponse.getFileName());

        logger.info("Saving file {} to {}", logResponse.getFileName(), outFile);
        if (!outFile.exists()) {
            try {
                FileUtils.forceMkdirParent(outFile);
            } catch (IOException e) {
                logger.error("Unable to create parent directories: {}", e.getMessage(), e);
            }
        }

        try (FileOutputStream fo = new FileOutputStream(outFile)) {
            IOUtils.copy(logResponse.getLogData(), fo);
        } catch (FileNotFoundException e) {
            logger.error("Unable to save the file: {}", e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Unable to save the file due to I/O error: {}", e.getMessage(), e);
        }

        verify(logResponse, outFile);
    }

    private void verify(LogResponse logResponse, File outFile) {
        if (logResponse.getFileHash() != null && !logResponse.getFileHash().isEmpty()) {
            try {
                logger.info("Verifying SHA-1 hash for file {}", outFile);
                if (!digest.verify(outFile.getPath(), logResponse.getFileHash())) {
                    logger.error("The SHA-1 hash for file {} does not match the expected one {}",
                            outFile.getPath(), logResponse.getFileHash());
                }
            } catch (IOException e) {
                logger.error("Unable to verify the hash for file {}: {}", outFile.getName(),
                        e.getMessage());
            }
        }
        else {
            logger.warn("The peer did not set up a hash for file {}", logResponse.getFileName());
        }
    }

    @Override
    public boolean call(MaestroNote note) {
        if (note instanceof LogResponse) {
            LogResponse logResponse = (LogResponse) note;
            if (logResponse.getLocationType() == LocationType.LAST_FAILED) {
                logger.info("About to download last failed reports from {}", logResponse.getPeerInfo().prettyName());
            }
            else {
                logger.info("About to download last successful reports from {}", logResponse.getPeerInfo().prettyName());
            }
            try {
                save(logResponse);
            }
            finally {
                downloadCount.incrementAndGet();
                monitor.doUnlock();
            }

            return false;
        }

        return true;
    }

    public int getDownloadCount() {
        return downloadCount.get();
    }
}
