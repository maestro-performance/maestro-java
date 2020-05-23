package org.maestro.reports.server.main;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import org.junit.Test;
import org.maestro.client.Maestro;
import org.maestro.worker.AbstractProtocolTest;
import org.maestro.worker.container.ArtemisContainer;
import org.maestro.worker.tests.support.annotations.MaestroPeer;
import org.maestro.worker.tests.support.annotations.ReceivingPeer;
import org.maestro.worker.tests.support.annotations.SendingPeer;
import org.maestro.worker.tests.support.runner.MiniPeer;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;


public class ReportsToolITest extends AbstractProtocolTest {

    @Rule
    public ArtemisContainer container = new ArtemisContainer();

    @ReceivingPeer
    private MiniPeer miniReceivingPeer;

    @SendingPeer
    private MiniPeer miniSendingPeer;

    @MaestroPeer
    private Maestro maestro;

    @MaestroPeer
    private DefaultToolLauncher defaultToolLauncher;

    private Future<Integer> reportsToolFuture;

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
        return 3;
    }

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

        String dataDirStr = this.getClass().getResource(".").getPath();
        File dataDir = new File(dataDirStr);

        defaultToolLauncher = new DefaultToolLauncher(dataDir, false, mqttEndpoint, "localhost");

        miniSendingPeer.start();
        miniReceivingPeer.start();

        ExecutorService executorService = Executors.newCachedThreadPool();

        reportsToolFuture = executorService.submit(() -> defaultToolLauncher.launchServices());

        System.out.println("Mini peers have started");
    }

    @After
    public void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
        stopWorkers(maestro);

        miniSendingPeer.stop();
        miniReceivingPeer.stop();
        maestro.halt();

        // TODO: need to check if the data was collected

        int ret = reportsToolFuture.get(5, TimeUnit.SECONDS);
        assertEquals(0, ret);
    }

    @Test(timeout = 300000)
    public void testFixedCountTest() throws Exception {
        String brokerAddress = container.getAMQPEndpoint();

        testFixedCountTest(maestro, brokerAddress + "/amqp.itest.queue");
    }

}
