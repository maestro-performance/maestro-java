package org.maestro.client.notes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.exceptions.MaestroException;
import org.msgpack.core.ExtensionTypeHeader;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.*;

public class LogResponse extends MaestroResponse {
    // Limit payload to 255 Mb because MQTT cannot have a payload bigger than 256Mb
    // 268435456

    // Use a conservative value of 100000000. ActiveMQ comes w/ 104857600
    // configured as the max frame size which limits the payload size here.
    private static final int LOG_RESPONSE_MAX_PAYLOAD_SIZE = 1000000;

    private LocationType locationType;
    private String fileName;
    private int index = 0;
    private int total;
    private int size;
    private long fileSize;

    private File file;

    private int pos = 0;
    private FileInputStream fi;
    private BufferedOutputStream bo;
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
                size = (int) fileSize - pos;
            }
        }
        packer.packInt(size);
        packer.packLong(fileSize);

        byte[] data = new byte[size];

        if (fi == null) {
            fi = new FileInputStream(file);
        }

        fi.skip(pos);
        fi.read(data, 0, size);
        packer.packBinaryHeader(size);
        packer.writePayload(data, 0, size);

        pos = pos + size;
        index++;

        if (index == total) {
            fi.close();
        }
    }

    public void join(final LogResponse logResponse) {
        if (bo == null) {
            bo = new BufferedOutputStream(new ByteArrayOutputStream());
        }

        if (logResponse.index == this.index + 1) {
            try {
                bo.write(logResponse.data);
                index++;
            } catch (IOException e) {
                throw new MaestroException("I/O error while trying to join log responses", e);
            }
        }
        else {
            throw new MaestroException("Out of order log response");
        }
    }

    public InputStream getLogData() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public boolean hasNext() {
        return index < total;
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
