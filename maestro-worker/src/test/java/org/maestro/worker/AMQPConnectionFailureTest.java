/*
 * Copyright 2019 Otavio Rodolfo Piske
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.worker;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.maestro.client.Maestro;
import org.maestro.common.LogConfigurator;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.worker.container.ArtemisContainer;
import org.maestro.worker.tests.support.annotations.MaestroPeer;
import org.maestro.worker.tests.support.annotations.ReceivingPeer;
import org.maestro.worker.tests.support.annotations.SendingPeer;
import org.maestro.worker.tests.support.runner.MiniPeer;

import java.util.List;

@Ignore
@SuppressWarnings("unused")
public class AMQPConnectionFailureTest extends AbstractProtocolTest {

    @Rule
    public ArtemisContainer container = new ArtemisContainer();

    @ReceivingPeer
    private MiniPeer miniReceivingPeer;

    @SendingPeer
    private MiniPeer miniSendingPeer;

    @MaestroPeer
    private Maestro maestro;

    @Before
    public void setUp() throws Exception {
        setupMaestroConnectionProperties();

        container.start();

        String amqpEndpoint = container.getAMQPEndpoint();
        System.out.println("Broker AMQP endpoint accessible at " + amqpEndpoint);

        String mqttEndpoint = container.getMQTTEndpoint();
        System.out.println("Broker MQTT endpoint accessible at " + mqttEndpoint);

        maestro = new Maestro(mqttEndpoint);

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

    @Override
    protected void validateDrain(Maestro maestro) {
        // NO-OP
    }

    @Override
    protected void validateTestResults(List<? extends MaestroNote> replies) {
        super.validateTestResultsFailure(replies);
    }

    @Test(timeout = 300000)
    public void testFixedCountTestWithConnectionError() throws Exception {
        LogConfigurator.silent();

        String brokerAddress = container.getAMQPEndpoint();

        testFixedCountTest(maestro, brokerAddress + "/unit.test.queue");
    }
}
