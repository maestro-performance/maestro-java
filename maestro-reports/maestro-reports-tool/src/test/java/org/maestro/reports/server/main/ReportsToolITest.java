package org.maestro.reports.server.main;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import org.junit.Test;
import org.maestro.client.Maestro;
import org.maestro.reports.dto.Report;
import org.maestro.reports.server.util.HTTPEasy;
import org.maestro.worker.AbstractProtocolTest;
import org.maestro.worker.container.ArtemisContainer;
import org.maestro.worker.tests.support.annotations.MaestroPeer;
import org.maestro.worker.tests.support.annotations.ReceivingPeer;
import org.maestro.worker.tests.support.annotations.SendingPeer;
import org.maestro.worker.tests.support.runner.MiniPeer;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class ReportsToolITest extends AbstractProtocolTest {
    private static final String API_HOST = "http://localhost:6500";

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

        int ret = reportsToolFuture.get(5, TimeUnit.SECONDS);
        assertEquals(0, ret);
    }

    @Test(timeout = 300000)
    public void testFixedCountTest() throws Exception {
        String brokerAddress = container.getAMQPEndpoint();

        testFixedCountTest(maestro, brokerAddress + "/amqp.itest.queue");

        // Check that the report server remains up after the test
        String apiLiveUrl = API_HOST + "/api/live";
        Response apiLiveResponse = HTTPEasy
                .url(apiLiveUrl)
                .get();

        assertEquals(HttpStatus.OK_200, apiLiveResponse.getStatus());

        String allReports = API_HOST + "/api/report/";
        Response allReportsResponse = HTTPEasy
                .url(allReports)
                .get();
        
        MappingJsonFactory factory = new MappingJsonFactory();
        JsonParser parser = factory.createParser((InputStream)allReportsResponse.getEntity());

        List<Report> reports = parser.readValueAs(new TypeReference<List<Report>>(){});

        for (Report report : reports) {
            assertNotNull(report);
            assertEquals("integration test", report.getTestName());
            assertEquals("junit", report.getTestScript());
            assertTrue(report.isValid());
            assertFalse(report.isAggregated());
            assertFalse(report.isRetired());
        }
    }

}
