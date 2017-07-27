/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.orpiske.mpt.maestro.client;

import net.orpiske.mpt.maestro.exceptions.MalformedNoteException;
import net.orpiske.mpt.maestro.notes.*;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class MaestroDeserializer {

    private static MaestroNotification deserializeNotification(MessageUnpacker unpacker, byte[] bytes) throws IOException, MalformedNoteException {
        long tmpCommand = unpacker.unpackLong();
        MaestroCommand command = MaestroCommand.from(tmpCommand);

        switch (command) {
            case MAESTRO_NOTE_NOTIFY_FAIL: {
                return new TestFailedNotification(unpacker);
            }
            case MAESTRO_NOTE_NOTIFY_SUCCESS: {
                return new TestSuccessfulNotification(unpacker);
            }
            default: {
                throw new MalformedNoteException("Invalid notification command: " + tmpCommand);
            }
        }
    }

    private static MaestroResponse deserializeResponse(MessageUnpacker unpacker, byte[] bytes) throws IOException, MalformedNoteException {
        long tmpCommand = unpacker.unpackLong();
        MaestroCommand command = MaestroCommand.from(tmpCommand);

        switch (command) {
            case MAESTRO_NOTE_OK: {
                return new OkResponse();
            }
            case MAESTRO_NOTE_PING: {
                return new PingResponse(unpacker);
            }
            default: {
                throw new MalformedNoteException("Invalid response command: " + tmpCommand);
            }
        }

    }

    private static MaestroRequest deserializeRequest(MessageUnpacker unpacker, byte[] bytes) throws IOException, MalformedNoteException {
        long tmpCommand = unpacker.unpackLong();
        MaestroCommand command = MaestroCommand.from(tmpCommand);

        switch (command) {
            case MAESTRO_NOTE_PING: {
                return new PingRequest(unpacker);
            }
            case MAESTRO_NOTE_FLUSH: {
                return new FlushRequest();
            }
            default: {
                throw new MalformedNoteException("Invalid request command: " + tmpCommand);
            }
        }
    }

    public static MaestroNote deserialize(byte[] bytes) throws IOException, MalformedNoteException {
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);

        try {
            short tmpType = unpacker.unpackShort();
            MaestroNoteType type = MaestroNoteType.from(tmpType);

            switch (type) {
                case MAESTRO_TYPE_REQUEST: return deserializeRequest(unpacker, bytes);
                case MAESTRO_TYPE_RESPONSE: return deserializeResponse(unpacker, bytes);
                case MAESTRO_TYPE_NOTIFICATION: return deserializeNotification(unpacker, bytes);
                default: throw new MalformedNoteException("Invalid note type: " + tmpType);
            }
        } finally {
            unpacker.close();
        }
    }
}
