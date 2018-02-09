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

package org.maestro.maestro;

import org.maestro.client.exchange.MaestroDeserializer;
import org.junit.Test;
import org.maestro.client.notes.*;

import static org.junit.Assert.assertTrue;

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


    @Test
    public void serializeOkResponse() throws Exception {
        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(new OkResponse()));

        assertTrue(parsed instanceof OkResponse);
        assertTrue("Parsed object is not a OK response",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_OK);
    }


    @Test
    public void serializeTestSuccessfulNotification() throws Exception {
        TestSuccessfulNotification tsn = new TestSuccessfulNotification();

        tsn.setId("asfas45");
        tsn.setName("unittest@localhost");
        tsn.setMessage("Test completed successfully");

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(tsn));

        assertTrue(parsed instanceof TestSuccessfulNotification);
        assertTrue("Parsed object is not a Test Successful Notification",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS);
    }

    @Test
    public void serializeTestFailedNotification() throws Exception {
        TestFailedNotification tsn = new TestFailedNotification();

        tsn.setId("asfas45");
        tsn.setName("unittest@localhost");
        tsn.setMessage("Test failed");

        MaestroNote parsed = MaestroDeserializer.deserialize(doSerialize(tsn));

        assertTrue(parsed instanceof TestFailedNotification);
        assertTrue("Parsed object is not a Test failed Notification",
                parsed.getNoteType() == MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertTrue(parsed.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_NOTIFY_FAIL);
    }
}
