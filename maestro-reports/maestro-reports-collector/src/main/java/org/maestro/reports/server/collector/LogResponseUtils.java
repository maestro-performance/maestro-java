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
 */

package org.maestro.reports.server.collector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.LogResponse;
import org.maestro.contrib.utils.digest.Sha1Digest;
import org.maestro.reports.common.organizer.DefaultOrganizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class LogResponseUtils {
    private static final Logger logger = LoggerFactory.getLogger(LogResponseUtils.class);


    public static void save(final LogResponse logResponse, final DefaultOrganizer organizer) {
        Objects.requireNonNull(logResponse);
        Objects.requireNonNull(organizer);

        final PeerInfo peerInfo = logResponse.getPeerInfo();
        final String uniquePeerPath = DefaultOrganizer.generateUniquePeerPath(logResponse.getId(), peerInfo);
        final String destinationDir = organizer.organize(uniquePeerPath);
        final File outFile = new File(destinationDir, logResponse.getFileName());

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

    private static void verify(final LogResponse logResponse, final File outFile) {
        Sha1Digest digest = new Sha1Digest();

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
}
