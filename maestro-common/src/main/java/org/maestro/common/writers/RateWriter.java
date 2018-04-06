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

package org.maestro.common.writers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * A writer class for performance rate data. This data is saved to a compressed file in the format
 * {role}-rate.gz
 */
public final class RateWriter implements AutoCloseable {
    private static final int MICROS_PART_LENGTH = "000\"".length();
    private static final String DATE_FORMAT_PATTERN = "\"yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * The timestamp formats
     */
    public static final String TIMESTAMP_FORMAT = "\"yyyy-MM-dd HH:mm:ss.SSSSSS\"";
    public static final char SEPARATOR = ',';

    /**
     * It include the {@code "} around the timestamps and {@code '\n'}.
     */
    public static final int ESTIMATED_LINE_LENGTH = ('\n' + DATE_FORMAT_PATTERN + SEPARATOR + DATE_FORMAT_PATTERN).length() + MICROS_PART_LENGTH * 2;
    //a more modern approach using DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.systemDefault())
    //could be considered but in the current state produces much more garbage
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    private final OutputStream outputStream;
    private final Date date = new Date();
    private final StringBuffer lineBuilder = new StringBuffer(ESTIMATED_LINE_LENGTH);
    private final byte[] writeBuffer = new byte[ESTIMATED_LINE_LENGTH];
    private final FieldPosition fullFieldPosition = new FieldPosition(DateFormat.FULL);
    private final File reportFile;

    public RateWriter(final File reportFolder, boolean sender, boolean compressed) throws IOException {
        final String role = sender ? "sender" : "receiver";
        final String fileName = role + (compressed ? "d-rate.csv.gz" : "d-rate.csv");
        this.reportFile = new File(reportFolder, fileName);

        outputStream = createOutputStream(compressed);

        writeHeader(sender);
    }

    private OutputStream createOutputStream(boolean compressed) throws IOException {
        final FileOutputStream fileStream = new FileOutputStream(reportFile);

        if (compressed) {
            return new GZIPOutputStream(fileStream);
        } else {
            return fileStream;
        }
    }

    private void writeHeader(boolean sender) throws IOException {
        //header depend on being sender/receiver
        final String firstTimeColumn;
        final String secondTimeColumn;

        if (sender) {
            firstTimeColumn = "etd";
            secondTimeColumn = "atd";
        } else {
            firstTimeColumn = "eta";
            secondTimeColumn = "ata";
        }

        final int encodedSize = encodeAscii(lineBuilder.append(firstTimeColumn).append(SEPARATOR).append(secondTimeColumn), writeBuffer);
        outputStream.write(writeBuffer, 0, encodedSize);
    }

    public File reportFile() {
        return reportFile;
    }

    public void write(long startTimeStampEpochMicros, long endTimeStampEpochMicros) {
        final int encodedSize = encodeAscii(appendOn(startTimeStampEpochMicros, endTimeStampEpochMicros), writeBuffer);
        try {
            outputStream.write(writeBuffer, 0, encodedSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int digitOf(long microseconds) {
        final int digits;
        if (microseconds < 10) {
            digits = 1;
        } else if (microseconds < 100) {
            digits = 2;
        } else {
            assert microseconds < 1000;
            digits = 3;
        }
        return digits;
    }

    private static StringBuffer appendDateMicros(long micros, StringBuffer buffer) {
        assert micros >= 0;
        if (micros == 0) {
            buffer.append("000");
            return buffer;
        }
        final int digits = digitOf(micros);
        final int zerosPrefix = 3 - digits;
        switch (zerosPrefix) {
            case 1:
                buffer.append("0");
                break;
            case 2:
                buffer.append("00");
                break;
        }
        buffer.append(micros);
        return buffer;
    }

    private StringBuffer appendOn(long startTimeStampEpochMicros, long endTimeStampEpochMicros) {
        final long startMillis = startTimeStampEpochMicros / 1000L;
        final long startRemainingMicros = startTimeStampEpochMicros % 1000L;
        final long endMillis = endTimeStampEpochMicros / 1000L;
        final long endRemainingMicros = endTimeStampEpochMicros % 1000L;

        lineBuilder.setLength(0);
        lineBuilder.append('\n');
        date.setTime(startMillis);
        dateFormat.format(date, lineBuilder, fullFieldPosition);
        appendDateMicros(startRemainingMicros, lineBuilder).append('"').append(SEPARATOR);
        date.setTime(endMillis);
        dateFormat.format(date, lineBuilder, fullFieldPosition);
        appendDateMicros(endRemainingMicros, lineBuilder).append('"');
        return lineBuilder;
    }

    private static int encodeAscii(StringBuffer buffer, byte[] encodedBuffer) {
        final int bufferLength = buffer.length();

        for (int i = 0; i < bufferLength; i++) {
            final char c = buffer.charAt(i);
            byte b = (byte) c;
            if (b < 0) {
                b = '?';
            }
            encodedBuffer[i] = b;
        }

        return bufferLength;
    }

    public void close() {
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
