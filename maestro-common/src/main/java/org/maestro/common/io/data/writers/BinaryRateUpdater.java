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

import org.maestro.common.Constants;
import org.maestro.common.Role;
import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.common.RateEntry;
import org.maestro.common.io.data.common.exceptions.InvalidHeaderValueException;
import org.maestro.common.io.data.common.exceptions.InvalidRecordException;
import org.maestro.common.io.data.readers.BinaryRateReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

/**
 * A utility class that can be used to update the contents of a Binary rate file
 *
 * It should not be used for performance-intensive operations.
 */
public class BinaryRateUpdater implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BinaryRateUpdater.class);

    private final FileChannel fileChannel;
    private FileHeader fileHeader;

    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(FileHeader.BYTES + 2);
    private final boolean overlay;

    /**
     * Constructor
     * @param reportFile the rate report file name
     * @throws IOException in case of I/O errors
     */
    public BinaryRateUpdater(final File reportFile) throws IOException {
        this(reportFile, true);
    }

    /**
     * Constructor
     * @param reportFile the rate report file name
     * @param overlay whether the update should overlay previous files. If false,
     *                the updater will create data files from the joined ones if
     *                the report file is not existent
     * @throws IOException in case of I/O errors
     */
    public BinaryRateUpdater(final File reportFile, boolean overlay) throws IOException {
        boolean exists = reportFile.exists();
        this.overlay = overlay;

        if (!overlay) {
            fileChannel = FileChannel.open(reportFile.toPath(), StandardOpenOption.READ,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        }
        else {
            fileChannel = FileChannel.open(reportFile.toPath(), StandardOpenOption.READ,
                    StandardOpenOption.WRITE);
        }

        if (exists) {
            fileHeader = readHeader();
        }
    }

    private boolean isOverlay() {
        return overlay;
    }

    /**
     * Gets the file header
     * @return the file header object
     */
    public FileHeader getFileHeader() {
        return fileHeader;
    }

    private FileHeader readHeader() throws IOException {
        byteBuffer.clear();
        int bytesRead = fileChannel.read(byteBuffer);
        if (bytesRead <= 0) {
            throw new InvalidHeaderValueException("The file does not contain a valid header");
        }

        logger.trace("Read {} bytes from the file channel", bytesRead);
        byteBuffer.flip();

        byte[] name = new byte[FileHeader.FORMAT_NAME_SIZE];
        byteBuffer.get(name, 0, FileHeader.FORMAT_NAME_SIZE);
        logger.trace("File format name: '{}'", new String(name));

        int fileVersion = byteBuffer.getInt();
        logger.trace("File version: '{}'", fileVersion);

        int maestroVersion = byteBuffer.getInt();
        logger.trace("Maestro version: '{}'", maestroVersion);

        Role role = Role.from(byteBuffer.getInt());
        logger.trace("Role: '{}'", role.getCode());

        return new FileHeader(new String(name), fileVersion, maestroVersion, role);
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


    /**
     * Updates the file header
     * @param header the file header
     * @throws IOException for multiple I/O related errors
     */
    public void updateHeader(final FileHeader header) throws IOException {
        byteBuffer.clear();
        byteBuffer.put(header.getFormatName().getBytes());
        byteBuffer.putInt(header.getFileVersion());
        byteBuffer.putInt(Constants.VERSION_NUMERIC);
        byteBuffer.putInt(header.getRole().getCode());

        fileChannel.position(0);
        write();

        this.fileHeader = header;
    }


    /**
     * Writes a performance entry to the file
     * @param older older entry
     * @param newer newer entry
     * @throws IOException for multiple I/O related errors
     */
    private void update(RateEntry older, RateEntry newer) throws IOException {
        int remaining = byteBuffer.remaining();

        if (logger.isTraceEnabled()) {
            logger.trace("Remaining: {}", remaining);
        }

        if (remaining < (Integer.BYTES + Long.BYTES + Long.BYTES)) {
            write();
        }

        long olderTs = TimeUnit.MICROSECONDS.toSeconds(older.getTimestamp());
        long newerTs = TimeUnit.MICROSECONDS.toSeconds(newer.getTimestamp());

        if (olderTs != newerTs) {
            logger.error("Cannot update records that are not within the same second slot: {} == {}", olderTs, newerTs);
            throw new InvalidRecordException("Cannot save multiple records for within the same second slot");
        }

        byteBuffer.putInt(older.getMetadata());
        byteBuffer.putLong(older.getCount() + newer.getCount());
        byteBuffer.putLong(newer.getTimestamp());
    }

    /**
     * Writes a performance entry to the file
     * @param entry entry to overlay a record for
     * @throws IOException for multiple I/O related errors
     */
    private void createEntry(RateEntry entry) throws IOException {
        int remaining = byteBuffer.remaining();

        if (logger.isTraceEnabled()) {
            logger.trace("Remaining: {}", remaining);
        }

        if (remaining < (Integer.BYTES + Long.BYTES + Long.BYTES)) {
            write();
        }

        byteBuffer.putInt(entry.getMetadata());
        byteBuffer.putLong(entry.getCount());
        byteBuffer.putLong(entry.getTimestamp());
    }

    public void update(RateEntry newer, long index) throws IOException {
        long pos = FileHeader.BYTES + (RateEntry.BYTES * index);

        fileChannel.position(pos);
        RateEntry older = readRecord(fileChannel, byteBuffer);

        byteBuffer.clear();
        fileChannel.position(pos);

        if (older != null) {
            update(older, newer);
        }
        else {
            if (!overlay) {
                createEntry(newer);
            }
        }
        write();
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


    private static RateEntry readRecord(FileChannel fileChannel, ByteBuffer byteBuffer) throws IOException {
        if (logger.isTraceEnabled()) {
            logBufferInfo(byteBuffer);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Read it all from the buffer. Fetching again from the channel");
        }

        byteBuffer.clear();
        int read = fileChannel.read(byteBuffer);
        if (read <= 0) {
            return null;
        }
        byteBuffer.flip();

        return new RateEntry(byteBuffer.getInt(), byteBuffer.getLong(), byteBuffer.getLong());
    }

    private static void logBufferInfo(final ByteBuffer byteBuffer) {
        logger.trace("Remaining: {}", byteBuffer.remaining());
        logger.trace("Position: {}", byteBuffer.position());
        logger.trace("Has Remaining: {}", byteBuffer.hasRemaining());
    }


    /**
     * Join a file another file on an previously opened updater
     * @param binaryRateUpdater the updater instance to join the file to
     * @param reportFile1 the file to be joined
     * @throws IOException for multiple I/O related errors
     */
    public static void joinFile(final BinaryRateUpdater binaryRateUpdater, final File reportFile1) throws IOException {
        try (BinaryRateReader reader = new BinaryRateReader(reportFile1)) {
            if (!binaryRateUpdater.isOverlay()) {
                FileHeader header = binaryRateUpdater.getFileHeader();
                if (header == null) {
                    binaryRateUpdater.updateHeader(reader.getHeader());
                }
            }

            RateEntry entry = reader.readRecord();
            long index = 0;
            while (entry != null) {
                binaryRateUpdater.update(entry, index);
                entry = reader.readRecord();
                index++;
            }
        }
    }
}
