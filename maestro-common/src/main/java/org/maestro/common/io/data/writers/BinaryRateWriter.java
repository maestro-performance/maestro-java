package org.maestro.common.io.data.writers;

import org.maestro.common.Constants;
import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.common.RateEntry;
import org.maestro.common.io.data.common.exceptions.InvalidRecordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

/**
 * Writes binary data files
 */
public class BinaryRateWriter implements RateWriter {
    private static final Logger logger = LoggerFactory.getLogger(BinaryRateWriter.class);

    private File reportFile;
    private FileChannel fileChannel;
    private long last = 0;

    // TODO: size needs to be adjusted accordingly
    private ByteBuffer byteBuffer = ByteBuffer.allocate(FileHeader.BYTES + (RateEntry.BYTES * 10));

    /**
     * Constructor
     * @param reportFile the rate report file name
     * @param fileHeader the file header
     * @throws IOException in case of I/O errors
     */
    public BinaryRateWriter(final File reportFile, final FileHeader fileHeader) throws IOException {
        this.reportFile = reportFile;

        fileChannel = new FileOutputStream(reportFile).getChannel();

        writeHeader(fileHeader);
    }

    private void write() throws IOException {
        byteBuffer.flip();

        while (byteBuffer.hasRemaining()) {
            // TODO: use the other
            fileChannel.write(byteBuffer);
        }

        byteBuffer.flip();
        byteBuffer.clear();
    }


    private void writeHeader(final FileHeader header) throws IOException {
        byteBuffer.clear();
        byteBuffer.put(header.getFormatName().getBytes());
        byteBuffer.putInt(header.getFileVersion());
        byteBuffer.putInt(Constants.VERSION_NUMERIC);
        byteBuffer.putInt(header.getRole().getCode());

        write();
    }


    @Override
    public File reportFile() {
        return reportFile;
    }

    /**
     * Writes a performance entry to the file
     * @param metadata entry metadata
     * @param count rate
     * @param timestamp timestamp of rate collection
     * @throws IOException
     */
    public void write(int metadata, long count, long timestamp) throws IOException {
        final int remaining = byteBuffer.remaining();

        if (remaining < RateEntry.BYTES) {
            if (logger.isTraceEnabled()) {
                logger.trace("There is not enough space on the buffer for a rate entry: {}", remaining);
            }

            write();
        }

        long now = TimeUnit.MICROSECONDS.toSeconds(timestamp);

        if (now == last) {
            logger.error("Cannot save multiple records for within the same second slot: {} == {}", now, last);
            throw new InvalidRecordException("Cannot save multiple records for within the same second slot");
        }

        if (now < last) {
            logger.error("Cannot save sequential record with a timestamp in the in the past: now {} < {}", now, last);
            throw new InvalidRecordException("Cannot save sequential record with a timestamp in the in the past");
        }

        byteBuffer.putInt(metadata);
        byteBuffer.putLong(count);
        byteBuffer.putLong(timestamp);
        last = now;
    }

    /**
     * Flushes the data to disk
     * @throws IOException in case of I/O errors
     */
    public void flush() throws IOException {
        write();
        fileChannel.force(true);
    }

    @Override
    public void close() {
        try {
            flush();
            fileChannel.close();
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(BinaryRateWriter.class);

            logger.error(e.getMessage(), e);
        }
    }
}
