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

package org.maestro.common.io.data.writers;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.maestro.common.Constants;
import org.maestro.common.Role;
import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.common.RateEntry;
import org.maestro.common.io.data.readers.BinaryRateReader;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BinaryRateUpdaterTest {
    private static void clean(final File reportFile) {
        if (reportFile != null && reportFile.exists()) {
            reportFile.delete();
        }
    }


    /**
     * Tests joining files with an existent destination file
     * @throws IOException for multiple types of I/O errors
     */
    @Test
    public void testJoinFile() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "sender.dat");
        File baseFile = new File(path, "sender-0.dat");

        FileUtils.copyFile(baseFile, reportFile);

        try (BinaryRateUpdater binaryRateUpdater = new BinaryRateUpdater(reportFile)) {
            File reportFile1 = new File(path, "sender-1.dat");
            BinaryRateUpdater.joinFile(binaryRateUpdater, reportFile1);

            File reportFile2 = new File(path, "sender-2.dat");
            BinaryRateUpdater.joinFile(binaryRateUpdater, reportFile2);

            try (BinaryRateReader reader = new BinaryRateReader(reportFile)) {

                FileHeader fileHeader = reader.getHeader();
                assertEquals(FileHeader.MAESTRO_FORMAT_NAME, fileHeader.getFormatName().trim());
                assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());

                // The file was generated w/ when the code was still marked as 1.3.8-SNAPSHOT
                assertEquals(138, fileHeader.getMaestroVersion());
                assertEquals(Role.SENDER, fileHeader.getRole());

                long count = 0;
                RateEntry entry = reader.readRecord();
                while (entry != null) {
                    count++;
                    assertEquals("Unexpected value", entry.getCount(), count * 3);
                    entry = reader.readRecord();
                }

                long total = 86400;
                assertEquals("The number of records don't match",
                        total, count);
            }
        }
        finally {
            clean(reportFile);
        }
    }


    /**
     * Tests joining files with a non-existent destination file
     * @throws IOException for multiple types of I/O errors
     */
    @Test
    public void testJoinFileOverlay() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "sender.dat");

        try (BinaryRateUpdater binaryRateUpdater = new BinaryRateUpdater(reportFile, false)) {

            File reportFile0 = new File(path, "sender-0.dat");
            BinaryRateUpdater.joinFile(binaryRateUpdater, reportFile0);

            File reportFile1 = new File(path, "sender-1.dat");
            BinaryRateUpdater.joinFile(binaryRateUpdater, reportFile1);

            File reportFile2 = new File(path, "sender-2.dat");
            BinaryRateUpdater.joinFile(binaryRateUpdater, reportFile2);

            try (BinaryRateReader reader = new BinaryRateReader(reportFile)) {
                FileHeader fileHeader = reader.getHeader();
                assertEquals(FileHeader.MAESTRO_FORMAT_NAME, fileHeader.getFormatName().trim());
                assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
                assertEquals(Constants.VERSION_NUMERIC, fileHeader.getMaestroVersion());
                assertEquals(Role.SENDER, fileHeader.getRole());

                long count = 0;
                RateEntry entry = reader.readRecord();
                while (entry != null) {
                    count++;
                    assertEquals("Unexpected value", entry.getCount(), count * 3);
                    entry = reader.readRecord();
                }

                long total = 86400;
                assertEquals("The number of records don't match",
                        total, count);
            }
        }
        finally {
            clean(reportFile);
        }
    }
}
