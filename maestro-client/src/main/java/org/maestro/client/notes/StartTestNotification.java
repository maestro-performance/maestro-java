package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class StartTestNotification extends MaestroNotification {
    private int localTestNumber;

    public StartTestNotification(int localTestNumber) {
        super(MaestroCommand.MAESTRO_NOTE_START_TEST);
        this.localTestNumber = localTestNumber;
    }

    public StartTestNotification(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_START_TEST, unpacker);
        this.localTestNumber = unpacker.unpackInt();
    }

    @Override
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packInt(localTestNumber);

        return packer;
    }
}
