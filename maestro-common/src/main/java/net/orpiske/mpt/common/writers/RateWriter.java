package net.orpiske.mpt.common.writers;

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
    //If was needed us it could be used: yyyy-MM-dd HH:mm:ss.SSS000
    private static final String DATE_FORMAT_PATTERN = "\"yyyy-MM-dd HH:mm:ss.SSS000\"";
    private static final char SEPARATOR = ',';
    private static final int ESTIMATED_LINE_LENGTH = (DATE_FORMAT_PATTERN + SEPARATOR + DATE_FORMAT_PATTERN + '\n').length();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    private final OutputStream outputStream;
    private final Date date = new Date();
    private final StringBuffer lineBuilder = new StringBuffer(ESTIMATED_LINE_LENGTH);
    private final byte[] writeBuffer = new byte[ESTIMATED_LINE_LENGTH];
    private final FieldPosition fullFieldPosition = new FieldPosition(DateFormat.FULL);

    public RateWriter(final File reportFolder, boolean sender, boolean compressed) throws IOException {
        final String role = sender ? "sender" : "receiver";
        final String fileName = role + (compressed ? "d-rate.csv.gz" : "d-rate,csv");
        final File reportFile = new File(reportFolder, fileName);
        final FileOutputStream fileStream = new FileOutputStream(reportFile);
        if (compressed) {
            outputStream = new GZIPOutputStream(fileStream);
        } else {
            outputStream = fileStream;
        }
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
        final int encodedSize = encodeAscii(lineBuilder.append(firstTimeColumn).append(SEPARATOR).append(secondTimeColumn).append('\n'), writeBuffer);
        outputStream.write(writeBuffer, 0, encodedSize);
    }

    public void write(long startTimeStampEpochMillis, long endTimeStampEpochMillis) {
        final int encodedSize = encodeAscii(appendOnStringBuffer(startTimeStampEpochMillis, endTimeStampEpochMillis).append('\n'), writeBuffer);
        try {
            outputStream.write(writeBuffer, 0, encodedSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StringBuffer appendOnStringBuffer(long startTimeStampEpochMillis, long endTimeStampEpochMillis) {
        lineBuilder.setLength(0);
        date.setTime(startTimeStampEpochMillis);

        dateFormat.format(date, lineBuilder, fullFieldPosition)
                .append(SEPARATOR);
        date.setTime(endTimeStampEpochMillis);
        return dateFormat.format(date, lineBuilder, fullFieldPosition);
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
