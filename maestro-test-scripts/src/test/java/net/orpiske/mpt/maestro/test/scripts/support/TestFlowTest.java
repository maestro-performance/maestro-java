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

import net.orpiske.jms.provider.activemq.ActiveMqProvider;
import net.orpiske.jms.test.annotations.Provider;
import net.orpiske.mpt.common.LogConfigurator;
import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.maestro.notes.MaestroCommand;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import net.orpiske.mpt.maestro.notes.MaestroNoteType;
import net.orpiske.mpt.maestro.tests.support.annotations.MaestroPeer;
import net.orpiske.mpt.maestro.tests.support.annotations.ReceivingPeer;
import net.orpiske.mpt.maestro.tests.support.annotations.SendingPeer;
import net.orpiske.mpt.maestro.tests.support.common.EndToEndTest;
import net.orpiske.mpt.maestro.tests.support.runner.MiniBrokerConfiguration;
import net.orpiske.mpt.maestro.tests.support.runner.MiniPeer;
import net.orpiske.mpt.maestro.tests.support.runner.WorkerTestRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(WorkerTestRunner.class)
@Provider(
        value = ActiveMqProvider.class,
        configuration = MiniBrokerConfiguration.class)
public class TestFlowTest extends EndToEndTest {

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

    /*
    TODO: this test is ignored because the workers are not yet sending the
    success/failure notifications
    */
    @Ignore
    @Test
    public void testSimpleTest() throws Exception {
        System.out.println("Running a short-lived test");

        maestro.setParallelCount(1);
        maestro.setDuration("5");
        maestro.setMessageSize(100);
        maestro.setFCL(1000);
        maestro.setRate(100);

        maestro.setBroker("amqp://localhost:5672/unit.test.queue");

        List<MaestroNote> replies = maestro.collect(1000, 10);

        // 12 = 6 commands * 2 peers (sending and receiving peers)
        assertTrue( "Replies don't match: " + replies.size(), replies.size() == 12);

        maestro.startSender();
        maestro.startReceiver();

        // Get the OK replies
        replies = maestro.collect(1000, 10, 2);

        Thread.sleep(2000);

        // Get the test result notification
        replies = maestro.collect(1000, 10, 2);
        assertTrue( "Replies don't match: " + replies.size(), replies.size() == 2);

        MaestroNote firstNote = replies.get(0);
        assertEquals(firstNote.getNoteType(), MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertEquals(firstNote.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS);

        MaestroNote secondNote = replies.get(1);
        assertEquals(secondNote.getNoteType(), MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertEquals(secondNote.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS);
    }
}
