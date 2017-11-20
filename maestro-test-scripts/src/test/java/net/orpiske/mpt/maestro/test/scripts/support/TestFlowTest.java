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

public class TestFlowTest extends EndToEndTest {
    protected static MiniBroker miniBroker;
    protected static MiniReceivingPeer miniReceivingPeer;
    protected static MiniSendingPeer miniSendingPeer;
    protected static Maestro maestro;

    @BeforeClass
    public static void setUp() throws Exception {
        LogConfigurator.silent();

        if (miniBroker == null) {
            miniBroker = new MiniBroker();

            miniBroker.start();
        }

        // TODO: probably there's a better way to do this
        while (!miniBroker.isStarted()) {
            System.out.println("Waiting for broker to start before starting the peer");
            Thread.sleep(1000);
        }

        if (miniReceivingPeer == null) {
            miniReceivingPeer = new MiniReceivingPeer();
        }

        miniReceivingPeer.start();

        if (miniSendingPeer == null) {
            miniSendingPeer = new MiniSendingPeer();
        }

        miniSendingPeer.start();

        if (maestro == null) {
            maestro = new Maestro("mqtt://localhost:1883");
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        miniReceivingPeer.stop();
        miniSendingPeer.stop();
        miniBroker.stop();
    }

    @Ignore
    @Test
    public void testSimpleTest() throws Exception {
        System.out.println("Running a short-lived test");

        maestro.setParallelCount(1);
        maestro.setDuration("100");
        maestro.setMessageSize(100);
        maestro.setFCL(1000);
        maestro.setRate(100);
        maestro.setBroker("amqp://localhost:5672/unit.test.queue");

        List<MaestroNote> replies = maestro.collect(1000, 10);

        // 12 = 6 commands * 2 peers (sending and receiving peers)
        assertTrue( "Replies don't match: " + replies.size(), replies.size() == 12);

        maestro.startSender();
        maestro.startReceiver();

        Thread.sleep(2000);

        replies = maestro.collect(1000, 10);
        assertTrue(replies.size() == 1);

        MaestroNote note = replies.get(0);
        assertEquals(note.getNoteType(), MaestroNoteType.MAESTRO_TYPE_NOTIFICATION);
        assertEquals(note.getMaestroCommand(), MaestroCommand.MAESTRO_NOTE_NOTIFY_SUCCESS);
    }
}
