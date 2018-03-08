package org.maestro.client.notes;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;

class AbtractMaestroNote implements MaestroNote {
    private MaestroNoteType noteType;
    private MaestroCommand maestroCommand;

    public AbtractMaestroNote(MaestroNoteType type, MaestroCommand command) {
        setNoteType(type);
        setMaestroCommand(command);
    }

    public MaestroNoteType getNoteType() {
        return noteType;
    }

    protected void setNoteType(MaestroNoteType noteType) {
        this.noteType = noteType;
    }

    public MaestroCommand getMaestroCommand() {
        return maestroCommand;
    }

    protected void setMaestroCommand(MaestroCommand maestroCommand) {
        this.maestroCommand = maestroCommand;
    }

    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();

        packer.packShort(noteType.getValue());
        packer.packLong(maestroCommand.getValue());

        return packer;
    }

    final public byte[] serialize() throws IOException {
        MessageBufferPacker packer = pack();

        packer.close();

        return packer.toByteArray();
    }

    @Override
    public String toString() {
        return "MaestroNote{" +
                "noteType=" + noteType +
                ", maestroCommand=" + maestroCommand +
                '}';
    }

}
