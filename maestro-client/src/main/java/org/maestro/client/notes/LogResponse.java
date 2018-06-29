package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class LogResponse extends MaestroResponse {
    private LocationType locationType;
    private String fileName;
    private int index;
    private int total;
    private int size;
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
        this.size = unpacker.unpackBinaryHeader();
        this.data = new byte[this.size];
        unpacker.readPayload(this.data);
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

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packInt(this.locationType.code);
        packer.packString(this.fileName);
        packer.packInt(this.index);
        packer.packInt(this.total);

        packer.packBinaryHeader(this.size);
        packer.writePayload(this.data);

        return packer;
    }

    @Override
    public String toString() {
        return "LogResponse{" +
                "name='" + fileName + '\'' +
                ", size=" + size +
                ", data=" + new String(data) + // Arrays.toString(data) +
                "} " + super.toString();
    }
}
