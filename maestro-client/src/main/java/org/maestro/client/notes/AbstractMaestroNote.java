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
import org.maestro.common.client.notes.MessageCorrelation;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

abstract class AbstractMaestroNote implements MaestroNote {
    private final MaestroNoteType noteType;
    private final MaestroCommand maestroCommand;
    private MessageCorrelation correlation;

    public AbstractMaestroNote(final MaestroNoteType noteType, final MaestroCommand maestroCommand) {
        this.noteType = noteType;
        this.maestroCommand = maestroCommand;
    }

    public AbstractMaestroNote(final MaestroNoteType type, final MaestroCommand command,
                               final MessageUnpacker unpacker) throws IOException {
        this(type, command);

        final String correlationId = unpacker.unpackString();
        final String messageId = unpacker.unpackString();

        correlation = new MessageCorrelation(correlationId, messageId);
    }

    public MaestroNoteType getNoteType() {
        return noteType;
    }


    public MaestroCommand getMaestroCommand() {
        return maestroCommand;
    }

    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();

        packer.packShort(noteType.getValue());
        packer.packLong(maestroCommand.getValue());

        correlate();

        packer.packString(correlation.getCorrelationId());
        packer.packString(correlation.getMessageId());

        return packer;
    }

    public MessageCorrelation correlate() {
        if (correlation == null) {
            correlation = MessageCorrelation.newRandomCorrelation();
        }

        return correlation;
    }

    public void correlate(final MessageCorrelation correlation) {
        this.correlation = correlation;
    }

    public void correlate(final MaestroNote note) {
        correlate(note.correlate());
    }

    public boolean correlatesTo(final MessageCorrelation correlation) {
        return this.correlation.equals(correlation);
    }

    public boolean correlatesTo(final MaestroNote note) {
        return correlation.equals(note.correlate());
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
