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
    // configured as the max frame chunkSize which limits the payload chunkSize here.
    private static final int LOG_RESPONSE_MAX_PAYLOAD_SIZE = 10000000;

    private LocationType locationType;
    private String fileName;
    private int index = 0;
    private int total;
    private long fileSize;

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

    protected void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int getTotal() {
        return total;
    }

    public int getChunkSize() {
        int ret;

        if (fileSize < LOG_RESPONSE_MAX_PAYLOAD_SIZE) {
            ret = (int) fileSize;
        }
        else {
            if ((pos + (long) LOG_RESPONSE_MAX_PAYLOAD_SIZE) < fileSize) {
                ret = LOG_RESPONSE_MAX_PAYLOAD_SIZE;
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
        setTotal(calculateBlockCount());
        setFileName(file.getName());
    }

    private int calculateBlockCount() {
        if (fileSize > LOG_RESPONSE_MAX_PAYLOAD_SIZE) {
            return (int) Math.ceil((double) fileSize / (double) LOG_RESPONSE_MAX_PAYLOAD_SIZE);
        }

        return 1;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packInt(this.locationType.code);
        packer.packString(this.fileName);
        packer.packInt(this.index);
        packer.packInt(this.total);

        packer.packLong(fileSize);

        packData(packer);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return packer;
    }


    protected InputStream initializeInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }


    protected void packData(final MessageBufferPacker packer) throws IOException {
        if (fi == null) {
            fi = initializeInputStream();
        }

        packData(packer, fi);
    }


    protected void packData(final MessageBufferPacker packer, final InputStream inputStream) throws IOException {
        logger.debug("Skipping {} bytes", pos);
        inputStream.skip(pos);

        int chunkSize = getChunkSize();
        byte[] data = new byte[chunkSize];
        inputStream.read(data, 0, chunkSize);
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
