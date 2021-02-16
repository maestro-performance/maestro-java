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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.maestro.client.Maestro;
import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.collector.MaestroCollector;
import org.maestro.client.resolver.MaestroClientResolver;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.worker.container.ArtemisContainer;
import org.maestro.worker.tests.support.annotations.MaestroPeer;
import org.maestro.worker.tests.support.annotations.ReceivingPeer;
import org.maestro.worker.tests.support.annotations.SendingPeer;
import org.maestro.worker.tests.support.runner.MiniPeer;

@SuppressWarnings("unused")
public class OpenwireITTest extends AbstractProtocolTest {

    @Rule
    public ArtemisContainer container = new ArtemisContainer();

    @ReceivingPeer
    private MiniPeer miniReceivingPeer;

    @SendingPeer
    private MiniPeer miniSendingPeer;

    @MaestroPeer
    private Maestro maestro;

    @Override
    protected int numWorkers() {
        return 2;
    }

    @Override
    protected int numReceivers() {
        return 1;
    }

    @Override
    protected int numSenders() {
        return 1;
    }

    @Override
    protected int numPeers() {
        return 2;
    }

    @Before
    public void setUp() throws Exception {
        setupMaestroConnectionProperties();

        container.start();

        String openWire = container.getOpenwireEndpoint();
        System.out.println("Broker OpenWire endpoint accessible at " + openWire);

        String mqttEndpoint = container.getMQTTEndpoint();
        System.out.println("Broker MQTT endpoint accessible at " + mqttEndpoint);

        MaestroClient client = MaestroClientResolver.newClient(mqttEndpoint);
        ConsumerEndpoint<MaestroNote> consumerEndpoint = MaestroClientResolver.newConsumerEndpoint(mqttEndpoint);

        MaestroCollector collector = new MaestroCollector(consumerEndpoint);

        maestro = new Maestro(collector, client);

        miniReceivingPeer = new MiniPeer("org.maestro.worker.jms.JMSReceiverWorker",
                mqttEndpoint, "receiver", "localhost");
        miniSendingPeer = new MiniPeer("org.maestro.worker.jms.JMSSenderWorker",
                mqttEndpoint, "sender", "localhost");

        miniSendingPeer.start();
        miniReceivingPeer.start();
        System.out.println("Mini peers have started");
    }

    @After
    public void tearDown() {
        stopWorkers(maestro);

        miniSendingPeer.stop();
        miniReceivingPeer.stop();
    }

    @Test(timeout = 300000)
    public void testFixedCount() throws Exception {
        String openWireAddr = container.getOpenwireEndpoint();

        testFixedCountTest(maestro, openWireAddr + "/openwire.itest.queue?protocol=OPENWIRE");
    }
}
