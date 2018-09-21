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

package org.maestro.client.notes;

import org.apache.commons.io.FileUtils;
import org.maestro.common.client.notes.LocationType;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.exceptions.MaestroException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class LogResponse extends MaestroData<MaestroLogCollectorListener> {
    private static final Logger logger = LoggerFactory.getLogger(LogResponse.class);
    // Limit payload to 255 Mb because MQTT cannot have a payload bigger than 256Mb
    // 268435456

    // Use a conservative value of 100000000. ActiveMQ comes w/ 104857600
    // configured as the max frame chunkSize which limits the payload chunkSize here.
    protected static final int LOG_RESPONSE_MAX_PAYLOAD_SIZE = 10000000;

    private LocationType locationType;
    private String fileName;
    private int index = 0;
    private int total;
    private long fileSize;
    // Can be empty default (let the caller decide)
    private String fileHash = "";

    private File file;

    private long pos = 0;
    private InputStream fi;
    private ByteArrayOutputStream bo;
    private byte[] data;

    public LogResponse() {
        super(MaestroCommand.MAESTRO_NOTE_LOG);
    }

    public LogResponse(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_LOG, unpacker);

        this.locationType = LocationType.byCode(unpacker.unpackInt());
        setFileName(unpacker.unpackString());
        setIndex(unpacker.unpackInt());
        setTotal(unpacker.unpackInt());
        setFileSize(unpacker.unpackLong());
        setFileHash(unpacker.unpackString());


        int chunkSize = unpacker.unpackBinaryHeader();
        data = new byte[chunkSize];
        unpacker.readPayload(data);
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getFileName() {
        return fileName;
    }

    protected void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    protected void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    protected void setTotal(int total) {
        this.total = total;
    }

    private void setIndex(int index) {
        this.index = index;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    private int getIndex() {
        return index;
    }

    private int getTotal() {
        return total;
    }

    protected int getChunkSize(int maxChunkSize) {
        int ret;

        if (fileSize < maxChunkSize) {
            ret = (int) fileSize;
        }
        else {
            if ((pos + (long) maxChunkSize) < fileSize) {
                ret = maxChunkSize;
            } else {
                ret = (int) (fileSize - pos);
            }
        }
        logger.debug("File {}/{} has {} bytes and is using {} of maximum payload chunkSize", getFileName(), index,
                fileSize, ret);

        return ret;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;

        setFileSize(FileUtils.sizeOf(file));
        setTotal(calculateBlockCount(LOG_RESPONSE_MAX_PAYLOAD_SIZE));
        setFileName(file.getName());
    }

    private int calculateBlockCount(int maxChunkSize) {
        if (fileSize > maxChunkSize) {
            return (int) Math.ceil((double) fileSize / (double) maxChunkSize);
        }

        return 1;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        try {
            packer.packInt(locationType.getCode());
            packer.packString(fileName);
            packer.packInt(index);
            packer.packInt(total);

            packer.packLong(fileSize);
            packer.packString(fileHash);

            packData(packer);
        } catch (Exception e) {
            packer.close();

            throw e;
        }

        return packer;
    }


    protected InputStream initializeInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }


    private void packData(final MessageBufferPacker packer) throws IOException {
        if (fi == null) {
            fi = initializeInputStream();
        }

        packData(packer, fi);
    }


    private void packData(final MessageBufferPacker packer, final InputStream inputStream) throws IOException {
        logger.debug("Skipping {} bytes", pos);

        int chunkSize = getChunkSize(LOG_RESPONSE_MAX_PAYLOAD_SIZE);
        byte[] data = new byte[chunkSize];
        final int read = inputStream.read(data, 0, chunkSize);
        if (read == -1) {
            logger.error("End of buffer has been reached");

            // TODO: check if needs to throw an exception
        }

        packer.packBinaryHeader(chunkSize);
        packer.writePayload(data, 0, chunkSize);

        pos = pos + chunkSize;

        if (!hasNext()) {
            logger.trace("Completed sending the file chunks. Closing the input stream");
            inputStream.close();
        }
    }

    public void join(final LogResponse logResponse) {
        if (logger.isTraceEnabled()) {
            logger.trace("Checking if the log response has chunks to be joined: {}", logResponse);
        }

        if (logResponse.index == (this.index + 1)) {
            try {
                if (bo == null) {
                    bo = new ByteArrayOutputStream();

                    bo.write(this.data);
                }

                logger.debug("Appending new log response chunk for file {} with index {}/{} to the current one {}",
                        this.getFileName(), logResponse.index, logResponse.total, this.index);

                bo.write(logResponse.data);

                this.pos = logResponse.pos;
                this.data = bo.toByteArray();
            } catch (IOException e) {
                throw new MaestroException("I/O error while trying to join log responses", e);
            }
        }
        else {
            logger.error("The file {}/{}/{} is not a sequence to the cached one {}/{}/{}",
                    logResponse.getFileName(), logResponse.getIndex(), logResponse.getTotal(),
                    this.getFileName(), this.getIndex(), this.getTotal());
            throw new MaestroException("Out of order log response");
        }
    }

    public InputStream getLogData() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public void next() {
        index++;
    }

    @Override
    public boolean hasNext() {
        return (index < total && total > 1);
    }

    public boolean isLast() {
        return (index == (total - 1));
    }

    @Override
    public void notify(MaestroLogCollectorListener visitor) {
        visitor.handle(this);
    }

    @Override
    public String toString() {
        return "LogResponse{" +
                "locationType=" + locationType +
                ", fileName='" + fileName + '\'' +
                ", index=" + index +
                ", total=" + total +
                ", file=" + file +
                ", fileSize=" + fileSize +
                ", pos=" + pos +
                "} " + super.toString();
    }
}
