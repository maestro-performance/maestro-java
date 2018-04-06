/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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

import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MaestroNoteType;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;

abstract class AbtractMaestroNote implements MaestroNote {
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
