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

package org.maestro.test.scripts.support;

import net.orpiske.jms.test.annotations.Provider;
import org.maestro.common.LogConfigurator;
import org.maestro.client.Maestro;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MaestroNoteType;

import net.orpiske.jms.provider.activemq.ActiveMqProvider;

import org.maestro.worker.tests.support.annotations.MaestroPeer;
import org.maestro.worker.tests.support.annotations.ReceivingPeer;
import org.maestro.worker.tests.support.annotations.SendingPeer;
import org.maestro.worker.tests.support.common.EndToEndTest;
import org.maestro.worker.tests.support.runner.MiniBrokerConfiguration;
import org.maestro.worker.tests.support.runner.MiniPeer;
import org.maestro.worker.tests.support.runner.WorkerTestRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(WorkerTestRunner.class)
@Provider(
        value = ActiveMqProvider.class,
        configuration = MiniBrokerConfiguration.class)
public class ScriptTest extends EndToEndTest {

    @ReceivingPeer
    protected MiniPeer miniReceivingPeer;

    @SendingPeer
    protected MiniPeer miniSendingPeer;

    @MaestroPeer
    protected Maestro maestro;

    @Before
    public void setUp() {
        LogConfigurator.silent();
    }

    @Test
    public void testPing() {
        System.out.println("Sending the ping request");
        maestro.pingRequest();

        List<MaestroNote> replies = maestro.collect(1000, 10, 2);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_PING);
    }

    @Test
    public void testSetFixedMessageSize() {
        System.out.println("Sending the set fixed message size request");
        maestro.setMessageSize(100);

        List<MaestroNote> replies = maestro.collect(1000, 10, 2);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetVariableMessageSize() {
        System.out.println("Sending the set variable message size request");
        maestro.setMessageSize("~100");

        List<MaestroNote> replies = maestro.collect(1000, 10, 2);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetBroker() {
        System.out.println("Sending the set broker request");
        maestro.setBroker("amqp://localhost/unit.test.queue");

        List<MaestroNote> replies = maestro.collect(1000, 10, 2);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetParallelCount() {
        System.out.println("Sending the set parallel count request");
        maestro.setParallelCount(100);

        List<MaestroNote> replies = maestro.collect(1000, 10, 2);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetFCL() {
        System.out.println("Sending the set fail condition request");
        maestro.setFCL(100);

        List<MaestroNote> replies = maestro.collect(1000, 10, 2);

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
    public void testStatsRequest() {
        System.out.println("Sending the stats request");
        maestro.statsRequest();

        List<MaestroNote> replies = maestro.collect(1000, 10, 2);

        assertTrue(replies.size() == 2);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_STATS);
    }
}
