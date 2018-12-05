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
import org.maestro.client.exchange.MaestroTopics;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;


@RunWith(WorkerTestRunner.class)
@Provider(
        value = ActiveMqProvider.class,
        configuration = MiniBrokerConfiguration.class)
public class ScriptTest extends EndToEndTest {

    @ReceivingPeer
    private MiniPeer miniReceivingPeer;

    @SendingPeer
    private MiniPeer miniSendingPeer;

    @MaestroPeer
    private Maestro maestro;

    @Before
    public void setUp() throws Exception {
        LogConfigurator.silent();
        System.setProperty("maestro.mqtt.no.reuse", "true");

        miniSendingPeer.start();
        miniReceivingPeer.start();
    }

    @After
    public void tearDown() {
        miniSendingPeer.stop();
        miniReceivingPeer.stop();
    }

    @Test
    public void testPing() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Sending the ping request");
        List<? extends MaestroNote> replies = maestro
                .pingRequest(MaestroTopics.WORKERS_TOPIC)
                .get(10, TimeUnit.SECONDS);

        assertEquals("Unexpected reply size", 2, replies.size());

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_PING);
    }

    @Test
    public void testSetFixedMessageSize() throws ExecutionException, InterruptedException, TimeoutException {
        System.out.println("Sending the set fixed message size request");
        List<? extends MaestroNote> replies = maestro
                .setMessageSize(MaestroTopics.WORKERS_TOPIC, 100)
                .get(10, TimeUnit.SECONDS);

        assertEquals(2, replies.size());

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetVariableMessageSize() throws ExecutionException, InterruptedException, TimeoutException {
        System.out.println("Sending the set variable message size request");
        List<? extends MaestroNote> replies = maestro.setMessageSize(MaestroTopics.WORKERS_TOPIC, "~100")
                .get(10, TimeUnit.SECONDS);

        assertEquals("Current size = " + replies.size(), 2, replies.size());

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetBroker() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Sending the set broker request");
        List<? extends MaestroNote> replies = maestro
                .setBroker(MaestroTopics.WORKERS_TOPIC, "amqp://localhost/unit.test.queue")
                .get(10, TimeUnit.SECONDS);

        assertEquals(2, replies.size());

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetParallelCount() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Sending the set parallel count request");
        List<? extends MaestroNote> replies = maestro
                .setParallelCount(MaestroTopics.WORKERS_TOPIC, 100)
                .get(10, TimeUnit.SECONDS);

        assertEquals(2, replies.size());

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    @Test
    public void testSetFCL() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Sending the set fail condition request");
        List<? extends MaestroNote> replies = maestro
                .setFCL(MaestroTopics.WORKERS_TOPIC, 100)
                .get(10, TimeUnit.SECONDS);

        assertEquals(2, replies.size());

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_OK);
    }

    /*
     * Deliberately marked as ignored because this feature is not yet complete
     */
    @Test
    public void testStatsRequest() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Sending the stats request");
        List<? extends MaestroNote> replies = maestro
                .statsRequest()
                .get(10, TimeUnit.SECONDS);

        assertEquals("Unexpected reply size", 2, replies.size());

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_RESPONSE);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_STATS);
    }
}
