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

package net.orpiske.mpt.maestro.test.scripts.support;

import net.orpiske.mpt.common.LogConfigurator;
import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.maestro.notes.MaestroCommand;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import net.orpiske.mpt.maestro.notes.MaestroNoteType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ScriptTest {
    private static MiniBroker miniBroker;
    private static MiniReceivingPeer miniReceivingPeer;
    private static MiniSendingPeer miniSendingPeer;
    private static Maestro maestro;

    @BeforeClass
    public static void setUp() throws Exception {
        LogConfigurator.silent();

        miniBroker = new MiniBroker();
        miniReceivingPeer = new MiniReceivingPeer();
        miniSendingPeer = new MiniSendingPeer();

        miniBroker.start();

        // TODO: probably there's a better way to do this
        while (!miniBroker.isStarted()) {
            System.out.println("Waiting for broker to start before starting the peer");
            Thread.sleep(1000);
        }

        miniReceivingPeer.start();
        miniSendingPeer.start();

        maestro = new Maestro("mqtt://localhost:1883");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        miniReceivingPeer.stop();
        miniSendingPeer.stop();
        miniBroker.stop();
    }

    @Test
    public void testPing() throws Exception {
        System.out.println("Sending the ping request");
        maestro.pingRequest();

        List<MaestroNote> replies = maestro.collect(1000, 10);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_PING);
    }

    @Test
    public void testSetFixedMessageSize() throws Exception {
        System.out.println("Sending the set fixed message size request");
        maestro.setMessageSize(100);

        List<MaestroNote> replies = maestro.collect(1000, 10);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetVariableMessageSize() throws Exception {
        System.out.println("Sending the set variable message size request");
        maestro.setMessageSize("~100");

        List<MaestroNote> replies = maestro.collect(1000, 10);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetBroker() throws Exception {
        System.out.println("Sending the set broker request");
        maestro.setBroker("amqp://localhost/unit.test.queue");

        List<MaestroNote> replies = maestro.collect(1000, 10);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetParallelCount() throws Exception {
        System.out.println("Sending the set parallel count request");
        maestro.setParallelCount(100);

        List<MaestroNote> replies = maestro.collect(1000, 10);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetFCL() throws Exception {
        System.out.println("Sending the set fail condition request");
        maestro.setFCL(100);

        List<MaestroNote> replies = maestro.collect(1000, 10);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    /*
     * Deliberately marked as ignored because this feature is not yet complete
     */
    @Ignore
    @Test
    public void testStatsRequest() throws Exception {
        System.out.println("Sending the stats request");
        maestro.statsRequest();

        List<MaestroNote> replies = maestro.collect(1000, 10);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_STATS);
    }
}
