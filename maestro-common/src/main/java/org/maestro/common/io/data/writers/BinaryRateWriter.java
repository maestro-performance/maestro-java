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

    private final File reportFile;
    private final FileChannel fileChannel;
    private long last = 0;

    // TODO: size needs to be adjusted accordingly
    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(FileHeader.BYTES + (RateEntry.BYTES * 10));

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
     * @throws IOException for multiple types of I/O errors
     */
    public void write(int metadata, long count, long timestamp) throws IOException {
        checkBufferCapacity();

        long now = TimeUnit.MICROSECONDS.toSeconds(timestamp);

        checkRecordTimeSlot(now);

        byteBuffer.putInt(metadata);
        byteBuffer.putLong(count);
        byteBuffer.putLong(timestamp);
        last = now;
    }

    private void checkBufferCapacity() throws IOException {
        final int remaining = byteBuffer.remaining();

        if (remaining < RateEntry.BYTES) {
            if (logger.isTraceEnabled()) {
                logger.trace("There is not enough space on the buffer for a rate entry: {}", remaining);
            }

            write();
        }
    }

    private void checkRecordTimeSlot(long now) {
        if (now <= last) {
            if (now < last) {
                logger.error("Cannot save sequential record with a timestamp in the in the past: now {} < {}", now, last);
                throw new InvalidRecordException("Cannot save sequential record with a timestamp in the in the past");
            }

            logger.error("Cannot save multiple records for within the same second slot: {} == {}", now, last);
            throw new InvalidRecordException("Cannot save multiple records for within the same second slot");
        }
        else {
            long next = last + 1;
            if (now != next && last != 0) {
                logger.warn("Trying to save a non-sequential record: now {} / expected {}", now, next);
            }
        }
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
