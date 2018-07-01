package org.maestro.client.notes;

import org.apache.commons.io.FileUtils;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.exceptions.MaestroException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class LogResponse extends MaestroResponse {
    private static final Logger logger = LoggerFactory.getLogger(LogResponse.class);
    // Limit payload to 255 Mb because MQTT cannot have a payload bigger than 256Mb
    // 268435456

    // Use a conservative value of 100000000. ActiveMQ comes w/ 104857600
    // configured as the max frame size which limits the payload size here.
    private static final int LOG_RESPONSE_MAX_PAYLOAD_SIZE = 10000000;

    private LocationType locationType;
    private String fileName;
    private int index = 0;
    private int total;
    private int size;
    private long fileSize;

    private File file;

    private long pos = 0;
    private FileInputStream fi;
    private ByteArrayOutputStream bo;
    private byte[] data;

    public LogResponse() {
        super(MaestroCommand.MAESTRO_NOTE_LOG);
    }

    public LogResponse(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_LOG, unpacker);

        this.locationType = LocationType.byCode(unpacker.unpackInt());
        this.fileName = unpacker.unpackString();
        this.index = unpacker.unpackInt();
        this.total = unpacker.unpackInt();
        this.size = unpacker.unpackInt();
        this.fileSize = unpacker.unpackLong();

        int dataSize = unpacker.unpackBinaryHeader();
        if (size != dataSize) {
            logger.warn("The note given size {} does not match the actual data size {}. Using the data size instead",
                    size, dataSize);

            size = dataSize;
        }

        data = new byte[this.size];
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

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public int getIndex() {
        return index;
    }

    public int getTotal() {
        return total;
    }

    public long getSize() {
        return size;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        fileSize = FileUtils.sizeOf(file);

        if (fileSize > LOG_RESPONSE_MAX_PAYLOAD_SIZE) {
            total = (int) Math.ceil((double) fileSize / (double) LOG_RESPONSE_MAX_PAYLOAD_SIZE);
        }
        else {
            total = 1;
        }
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packInt(this.locationType.code);
        packer.packString(this.fileName);
        packer.packInt(this.index);
        packer.packInt(this.total);

        packData(packer);

        return packer;
    }

    private void packData(MessageBufferPacker packer) throws IOException {
        if (fileSize < LOG_RESPONSE_MAX_PAYLOAD_SIZE) {
            size = (int) fileSize;
        }
        else {
            if ((pos + LOG_RESPONSE_MAX_PAYLOAD_SIZE) < fileSize) {
                size = LOG_RESPONSE_MAX_PAYLOAD_SIZE;
            } else {
                size = (int) (fileSize - pos);
            }
        }
        logger.debug("File {}/{} has {} bytes and is using {} of maximum payload size", file.getName(), index,
                fileSize, size);

        packer.packInt(size);
        packer.packLong(fileSize);

        byte[] data = new byte[size];

        if (fi == null) {
            fi = new FileInputStream(file);
        }

        if (pos > 0) {
            fi.skip(pos);
        }

        fi.read(data, 0, size);
        packer.packBinaryHeader(size);
        packer.writePayload(data, 0, size);

        pos = pos + size;

        if (!hasNext()) {
            logger.trace("Completed sending the file chunks. Closing the input stream");
            fi.close();
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
    public String toString() {
        return "LogResponse{" +
                "locationType=" + locationType +
                ", fileName='" + fileName + '\'' +
                ", index=" + index +
                ", total=" + total +
                ", size=" + size +
                ", file=" + file +
                ", fileSize=" + fileSize +
                ", pos=" + pos +
                "} " + super.toString();
    }
}
