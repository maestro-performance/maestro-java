package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class LogRequest extends MaestroRequest<MaestroEventListener> {
    private LocationType locationType;
    private String typeName;

    public LogRequest() {
        super(MaestroCommand.MAESTRO_NOTE_LOG);
    }

    public LogRequest(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_LOG);

        this.locationType = LocationType.byCode(unpacker.unpackInt());
        if (locationType == LocationType.ANY) {
            this.typeName = unpacker.unpackString();
        }
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packInt(this.locationType.code);

        if (locationType == LocationType.ANY) {
            packer.packString(this.typeName);
        }

        return packer;
    }


    @Override
    public String toString() {
        return "LogRequest{" +
                "locationType=" + locationType +
                ", typeName='" + typeName + '\'' +
                "} " + super.toString();
    }
}
