/*
 *  Copyright ${YEAR} ${USER}
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

package net.orpiske.mpt.maestro;

import net.orpiske.mpt.maestro.exceptions.MalformedNoteException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class MaestroDeserializer {

    private static MaestroNotification deserializeNotification(MessageUnpacker unpacker, byte[] bytes) throws IOException {
        return null;
    }

    private static MaestroResponse deserializeResponse(MessageUnpacker unpacker, byte[] bytes) throws IOException {
        return null;
    }

    private static MaestroRequest deserializeRequest(MessageUnpacker unpacker, byte[] bytes) throws IOException {
        MaestroCommand command = MaestroCommand.from(unpacker.unpackLong());

        switch (command) {
            case MAESTRO_NOTE_PING: {
                return new PingRequest(unpacker, bytes);
            }
            case MAESTRO_NOTE_FLUSH: {
                return new FlushRequest();
            }
            default: {
                break;
            }
        }


        return null;

    }

    public static MaestroNote deserialize(byte[] bytes) throws IOException, MalformedNoteException {
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);

        try {
            MaestroNoteType type = MaestroNoteType.from(unpacker.unpackShort());

            switch (type) {
                case MAESTRO_TYPE_REQUEST: return deserializeRequest(unpacker, bytes);
                case MAESTRO_TYPE_RESPONSE: return deserializeResponse(unpacker, bytes);
                case MAESTRO_TYPE_NOTIFICATION: return deserializeNotification(unpacker, bytes);
                default: throw new MalformedNoteException("Invalid note type");
            }
        } finally {
            unpacker.close();
        }
    }
}
