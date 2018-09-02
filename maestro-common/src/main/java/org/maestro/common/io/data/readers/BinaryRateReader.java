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

package org.maestro.common.io.data.readers;

import org.maestro.common.Role;
import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.common.RateEntry;
import org.maestro.common.io.data.common.exceptions.InvalidHeaderValueException;
import org.maestro.common.io.data.writers.BinaryRateWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BinaryRateReader implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BinaryRateReader.class);

    private final FileChannel fileChannel;
    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(FileHeader.BYTES + (RateEntry.BYTES * 20));

    private final FileHeader fileHeader;

    static {
        assert ((FileHeader.BYTES % RateEntry.BYTES) == 0):
                "File header and the rate entries must be aligned on a 20 bytes boundary";
    }


    /**
     * Constructor
     * @param fileName the report file name
     * @throws IOException in case of I/O errors
     */
    public BinaryRateReader(final File fileName) throws IOException {
        fileChannel = new FileInputStream(fileName).getChannel();

        fileHeader = readHeader();
    }

    /**
     * Gets the file header
     * @return the file header
     */
    public FileHeader getHeader() {
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

    /**
     * Read an entry from the file
     * @return An rate entry from the file or null on end-of-file
     * @throws IOException if unable to read the entry
     */
    public RateEntry readRecord() throws IOException {
        if (logger.isTraceEnabled()) {
            logBufferInfo();
        }

        if (byteBuffer.hasRemaining()) {
            return new RateEntry(byteBuffer.getInt(), byteBuffer.getLong(), byteBuffer.getLong());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Read it all from the buffer. Fetching again from the channel");
        }

        byteBuffer.compact();
        int read = fileChannel.read(byteBuffer);
        if (read <= 0) {
            return null;
        }
        byteBuffer.flip();

        return new RateEntry(byteBuffer.getInt(), byteBuffer.getLong(), byteBuffer.getLong());
    }

    private void logBufferInfo() {
        logger.trace("Remaining: {}", byteBuffer.remaining());
        logger.trace("Position: {}", byteBuffer.position());
        logger.trace("Has Remaining: {}", byteBuffer.hasRemaining());
    }

    /**
     * Close the reader and release resources
     */
    @Override
    public void close() {
        try {
            fileChannel.close();
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(BinaryRateWriter.class);

            logger.error(e.getMessage(), e);
        }
    }
}
