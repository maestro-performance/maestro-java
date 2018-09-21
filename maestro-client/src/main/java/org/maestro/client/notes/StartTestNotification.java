package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class StartTestNotification extends MaestroNotification {
    private Test test;

    public StartTestNotification(final Test test) {
        super(MaestroCommand.MAESTRO_NOTE_START_TEST);
        this.test = test;
    }

    public StartTestNotification(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_START_TEST, unpacker);

        int testNumber = unpacker.unpackInt();
        int testIteration = unpacker.unpackInt();
        String testName = unpacker.unpackString();

        this.test = new Test(testNumber, testIteration, testName);
    }

    @Override
    public void notify(MaestroEventListener visitor) {
        visitor.handle(this);
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packInt(test.getTestNumber());
        packer.packInt(test.getTestIteration());
        packer.packString(test.getTestName());

        return packer;
    }
}
