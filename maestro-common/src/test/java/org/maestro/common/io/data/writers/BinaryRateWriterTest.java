package org.maestro.common.io.data.writers;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.maestro.common.Constants;
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

            BinaryRateReader reader = new BinaryRateReader(reportFile);

            FileHeader fileHeader = reader.getHeader();
            assertEquals(FileHeader.MAESTRO_FORMAT_NAME, fileHeader.getFormatName().trim());
            assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
            assertEquals(Constants.VERSION_NUMERIC, fileHeader.getMaestroVersion());
            assertEquals(FileHeader.Role.SENDER, fileHeader.getRole());
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

            BinaryRateReader reader = new BinaryRateReader(reportFile);

            FileHeader fileHeader = reader.getHeader();
            assertEquals(FileHeader.MAESTRO_FORMAT_NAME, fileHeader.getFormatName().trim());
            assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
            assertEquals(Constants.VERSION_NUMERIC, fileHeader.getMaestroVersion());
            assertEquals(FileHeader.Role.SENDER, fileHeader.getRole());
        }
        finally {
            clean(reportFile);
        }
    }

    private void generateDataFileRandom(File reportFile) throws IOException {
        BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_SENDER);

        EpochMicroClock clock = EpochClocks.exclusiveMicro();
        long now = clock.microTime();

        for (int i = 0; i < TimeUnit.DAYS.toSeconds(1); i++) {
            binaryRateWriter.write(1, Double.valueOf(Math.random()).longValue(), now);

            now += TimeUnit.SECONDS.toMicros(1);
        }

        binaryRateWriter.close();
    }

    @Test
    public void testHeaderReadWriteRecords() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "testHeaderReadWriteRecords.dat");

        try {
            long total = generateDataFilePredictable(reportFile);

            BinaryRateReader reader = new BinaryRateReader(reportFile);

            FileHeader fileHeader = reader.getHeader();
            assertEquals(FileHeader.MAESTRO_FORMAT_NAME, fileHeader.getFormatName().trim());
            assertEquals(FileHeader.CURRENT_FILE_VERSION, fileHeader.getFileVersion());
            assertEquals(Constants.VERSION_NUMERIC, fileHeader.getMaestroVersion());
            assertEquals(FileHeader.Role.SENDER, fileHeader.getRole());

            long count = 0;
            RateEntry entry = reader.readRecord();
            while (entry != null) {
                count++;
                entry = reader.readRecord();
            }

            assertEquals("The number of records don't match",
                    total, count);
        }
        finally {
            clean(reportFile);
        }
    }

    private long generateDataFilePredictable(File reportFile) throws IOException {
        BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_SENDER);

        EpochMicroClock clock = EpochClocks.exclusiveMicro();

        long total = TimeUnit.DAYS.toSeconds(1);

        long now = clock.microTime();

        for (int i = 0; i < total; i++) {
            binaryRateWriter.write(0, i + 1, now);

            now += TimeUnit.SECONDS.toMicros(1);
        }

        binaryRateWriter.close();
        return total;
    }

    @Test(expected = InvalidRecordException.class)
    public void testHeaderWriteRecordsNonSequential() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportFile = new File(path, "testHeaderWriteRecordsNonSequential.dat");

        try {
            BinaryRateWriter binaryRateWriter = new BinaryRateWriter(reportFile, FileHeader.WRITER_DEFAULT_SENDER);

            EpochMicroClock clock = EpochClocks.exclusiveMicro();

            long total = TimeUnit.DAYS.toSeconds(1);

            long now = clock.microTime();

            for (int i = 0; i < total; i++) {
                binaryRateWriter.write(0, i + 1, now);

                now -= TimeUnit.SECONDS.toMicros(1);
            }

            binaryRateWriter.close();
        }
        finally {
            clean(reportFile);
        }
    }
}


