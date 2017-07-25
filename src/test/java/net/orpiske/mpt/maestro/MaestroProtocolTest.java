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

import net.orpiske.mpt.maestro.client.MaestroDeserializer;
import net.orpiske.mpt.maestro.notes.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MaestroProtocolTest {

    private byte[] doSerialize(MaestroNote note) throws Exception {
        return note.serialize();
    }


    @Test
    public void serializePingRequest() throws Exception {
        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(new PingRequest()));

        assertTrue(parsed instanceof PingRequest);
        assertTrue("Parsed object is not a ping request",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_PING);

        assertTrue(((PingRequest) parsed).getSec() != 0);
        assertTrue(((PingRequest) parsed).getUsec() != 0);
    }

    @Test
    public void serializeFlushRequest() throws Exception {
        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(new FlushRequest()));

        assertTrue("Parsed object is not a flush request",
                parsed instanceof FlushRequest);

        assertTrue(parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_REQUEST);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_FLUSH);
    }
}
