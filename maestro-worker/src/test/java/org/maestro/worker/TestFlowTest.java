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

package org.maestro.worker;

import net.orpiske.jms.provider.activemq.ActiveMqProvider;
import net.orpiske.jms.test.annotations.Provider;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.common.LogConfigurator;
import org.maestro.client.Maestro;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MaestroNoteType;
import org.maestro.common.client.notes.WorkerStartOptions;
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
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
@RunWith(WorkerTestRunner.class)
@Provider(
        value = ActiveMqProvider.class,
        configuration = MiniBrokerConfiguration.class)
public class TestFlowTest extends EndToEndTest {

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
    public void testSimpleTest() throws Exception {
        System.out.println("Running a short-lived test");

        List<? extends MaestroNote> set1 = maestro.setParallelCount(MaestroTopics.WORKERS_TOPIC, 1).get();
        assertEquals("Set parallel count replies don't match: " + set1.size(), 2, set1.size());

        List<? extends MaestroNote> set2 = maestro.setDuration(MaestroTopics.WORKERS_TOPIC, "5").get();
        assertEquals("Set duration replies don't match: " + set2.size(), 2, set2.size());

        List<? extends MaestroNote> set3 = maestro.setMessageSize(MaestroTopics.WORKERS_TOPIC, 100).get();
        assertEquals("Set message size replies don't match: " + set3.size(), 2, set3.size());

        List<? extends MaestroNote> set4 = maestro.setFCL(MaestroTopics.WORKERS_TOPIC, 1000).get();
        assertEquals("Set FCL replies don't match: " + set4.size(), 2, set4.size());

        List<? extends MaestroNote> set5 = maestro.setRate(MaestroTopics.WORKERS_TOPIC, 100).get();
        assertEquals("Set rate replies don't match: " + set5.size(), 2, set5.size());

        List<? extends MaestroNote> set6 = maestro
                .setBroker(MaestroTopics.WORKERS_TOPIC, "amqp://localhost:5672/unit.test.queue")
                .get();
        assertEquals("Set broker replies don't match: " + set6.size(), 2, set6.size());


        // 12 = 6 commands * 2 peers (sending and receiving peers)

        maestro.startWorker(MiniPeer.RECEIVER_TOPIC, new WorkerStartOptions("JmsReceiver"));
        maestro.startWorker(MiniPeer.SENDER_TOPIC, new WorkerStartOptions("JmsSender"));


        // Get the test result notification
        List<? extends MaestroNote> replies = maestro
                .waitForNotifications(2)
                .get(3, TimeUnit.SECONDS);

        assertEquals("Replies don't match: " + replies.size(), 2, replies.size());

        MaestroNote firstNote = replies.get(0);
        assertEquals(firstNote.getNoteType(), MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertEquals(firstNote.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS);

        MaestroNote secondNote = replies.get(1);
        assertEquals(secondNote.getNoteType(), MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertEquals(secondNote.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS);
    }
}
