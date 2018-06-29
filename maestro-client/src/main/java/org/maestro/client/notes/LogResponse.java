package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.Arrays;

public class LogResponse extends MaestroResponse {
    private String name;
    private int size;
    private byte[] data;

    public LogResponse() {
        super(MaestroCommand.MAESTRO_NOTE_LOG);
    }

    public LogResponse(MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_LOG, unpacker);

        this.name = unpacker.unpackString();
        this.size = unpacker.unpackBinaryHeader();
        this.data = new byte[this.size];
        unpacker.readPayload(this.data);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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

        packer.packString(this.name);
        packer.packBinaryHeader(this.size);
        packer.writePayload(this.data);

        return packer;
    }

    @Override
    public String toString() {
        return "LogResponse{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", data=" + new String(data) + // Arrays.toString(data) +
                "} " + super.toString();
    }
}
