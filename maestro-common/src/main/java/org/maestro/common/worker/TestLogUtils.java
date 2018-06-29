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

package org.maestro.common.worker;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * A set of worker log utilities.
 *
 */
public class TestLogUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestLogUtils.class);

    public static File findLastLogDir(final File logDir) {
        File currentLogDir = new File(logDir, "0");
        File lastLogDir = currentLogDir;
        int count = 0;

        while (currentLogDir.exists()) {
            lastLogDir = currentLogDir;

            count++;
            currentLogDir = new File(logDir, Integer.toString(count));
        }

        return lastLogDir;
    }

    public static File lastTestLogDir(final File logDir) {
        return new File(logDir, "last");
    }

    public static File lastFailedTestLogDir(final File logDir) {
        return new File(logDir, "lastFailed");
    }

    public static File lastSuccessfulTestLogDir(final File logDir) {
        return new File(logDir, "lastSuccessful");
    }

    public static File anyTestLogDir(final File logDir, final String name) {
        return new File(logDir, name);
    }

    public static File nextTestLogDir(final File logDir) {
        File testLogDir = new File(logDir, "0");
        int count = 0;

        while (testLogDir.exists()) {
            count++;
            testLogDir = new File(logDir, Integer.toString(count));

        }

        testLogDir.mkdirs();
        return testLogDir;
    }

    private static void deleteLinkQuietly(final File lastLink) {
        logger.debug("Deleting link {} after completing the test", lastLink.getName());

        try {
            FileUtils.deleteDirectory(lastLink);
        } catch (IOException e) {
            logger.warn("Unable to delete last link: {}", e.getMessage());
        }
    }

    public static void createSymlinks(final File logDir, boolean failed) {
        logger.trace("Creating the symlinks");
        File lastLogDir = findLastLogDir(logDir);
        Path target = Paths.get(lastLogDir.getAbsolutePath());

        Path lastLink = Paths.get(lastLogDir.getParent() + File.separator + "last");
        deleteLinkQuietly(lastLink.toFile());

        try {
            logger.trace("Updating the 'last' link");
            Files.createSymbolicLink(lastLink, target);
        } catch (IOException e) {
            logger.trace("Symbolic link creation error: {}", e.getMessage(), e);
        }

        if (failed) {
            Path lastFailedLink = Paths.get(lastLogDir.getParent() + File.separator + "lastFailed");
            deleteLinkQuietly(lastFailedLink.toFile());

            try {
                logger.trace("Updating the 'lastFailed' link");
                Files.createSymbolicLink(lastFailedLink, target);
            } catch (IOException e) {
                logger.trace("Symbolic link creation error: {}", e.getMessage(), e);
            }
        }
        else {
            Path lastSuccessfulLink = Paths.get(lastLogDir.getParent() + File.separator + "lastSuccessful");
            deleteLinkQuietly(lastSuccessfulLink.toFile());

            try {
                logger.trace("Updating the 'lastSuccessful' link");
                Files.createSymbolicLink(lastSuccessfulLink, target);
            } catch (IOException e) {
                logger.trace("Symbolic link creation error: {}", e.getMessage(), e);
            }
        }
    }
}
