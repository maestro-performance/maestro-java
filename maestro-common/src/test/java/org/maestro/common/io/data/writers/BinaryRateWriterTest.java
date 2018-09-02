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

package org.maestro.common.io.data.writers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.maestro.common.Constants;
import org.maestro.common.Role;
import org.maestro.common.duration.EpochClocks;
import org.maestro.common.duration.EpochMicroClock;
import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.common.RateEntry;
import org.maestro.common.io.data.common.exceptions.InvalidRecordException;
import org.maestro.common.io.data.readers.BinaryRateReader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BinaryRateWriterTest {

    private static void clean(final File reportFile) {
        if (reportFile != null && reportFile.exists()) {
            reportFile.delete();
        }
    }

    @Test
    public void testHeader() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "testHeader.dat");

        try {
            BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_SENDER);

            binaryRateWriter.close();

            try (BinaryRateReader reader = new BinaryRateReader(reportFile)) {

                FileHeader fileHeader = reader.getHeader();
                assertEquals(FileHeader.MAESTRO_FORMAT_NAME, fileHeader.getFormatName().trim());
                assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
                assertEquals(Constants.VERSION_NUMERIC, fileHeader.getMaestroVersion());
                assertEquals(Role.SENDER, fileHeader.getRole());
            }
        }
        finally {
            clean(reportFile);
        }
    }

    @Test
    public void testHeaderWriteRecords() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "testHeaderWriteRecords.dat");

        try {
            generateDataFileRandom(reportFile);

            try (BinaryRateReader reader = new BinaryRateReader(reportFile)) {

                FileHeader fileHeader = reader.getHeader();
                assertEquals(FileHeader.MAESTRO_FORMAT_NAME, fileHeader.getFormatName().trim());
                assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
                assertEquals(Constants.VERSION_NUMERIC, fileHeader.getMaestroVersion());
                assertEquals(Role.SENDER, fileHeader.getRole());
            }
        }
        finally {
            clean(reportFile);
        }
    }

    private void generateDataFileRandom(File reportFile) throws IOException {
        try (BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_SENDER)) {

            EpochMicroClock clock = EpochClocks.exclusiveMicro();
            long now = clock.microTime();

            for (int i = 0; i < TimeUnit.DAYS.toSeconds(1); i++) {
                binaryRateWriter.write(1, Double.valueOf(Math.random()).longValue(), now);

                now += TimeUnit.SECONDS.toMicros(1);
            }
        }
    }

    @Test
    public void testHeaderReadWriteRecords() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "testHeaderReadWriteRecords.dat");

        try {
            long total = generateDataFilePredictable(reportFile);

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
                    entry = reader.readRecord();
                }

                assertEquals("The number of records don't match",
                        total, count);
            }
        }
        finally {
            clean(reportFile);
        }
    }

    private long generateDataFilePredictable(File reportFile) throws IOException {
        try (BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_SENDER)) {

            EpochMicroClock clock = EpochClocks.exclusiveMicro();

            long total = TimeUnit.DAYS.toSeconds(1);

            long now = clock.microTime();

            for (int i = 0; i < total; i++) {
                binaryRateWriter.write(0, i + 1, now);

                now += TimeUnit.SECONDS.toMicros(1);
            }

            return total;
        }
    }

    @Test(expected = InvalidRecordException.class)
    public void testHeaderWriteRecordsNonSequential() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "testHeaderWriteRecordsNonSequential.dat");

        try (BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_SENDER)) {
            EpochMicroClock clock = EpochClocks.exclusiveMicro();

            long total = TimeUnit.DAYS.toSeconds(1);

            long now = clock.microTime();

            for (int i = 0; i < total; i++) {
                binaryRateWriter.write(0, i + 1, now);

                now -= TimeUnit.SECONDS.toMicros(1);
            }
        }
        finally {
            clean(reportFile);
        }
    }
}


