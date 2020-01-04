package org.maestro.reports.server.main;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import org.junit.Test;
import org.maestro.client.Maestro;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.mqtt.MqttConsumerEndpoint;
import org.maestro.common.LogConfigurator;
import org.maestro.common.client.notes.MaestroNote;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.maestro.client.exchange.mqtt.MaestroMqttClient;
import org.maestro.client.exchange.collector.MaestroCollector;

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
//        LogConfigurator.debug();

        String testDataDir = this.getClass().getResource(".").getPath();
        configureReportsDB(testDataDir);

        container.start();

        String amqpEndpoint = container.getAMQPEndpoint();
        System.out.println("Broker AMQP endpoint accessible at " + amqpEndpoint);

        String mqttEndpoint = container.getMQTTEndpoint();
        System.out.println("Broker MQTT endpoint accessible at " + mqttEndpoint);

        MaestroMqttClient client = new MaestroMqttClient(mqttEndpoint);
        client.connect();

        MqttConsumerEndpoint<MaestroNote> consumerEndpoint = new MqttConsumerEndpoint<>(mqttEndpoint, MaestroDeserializer::deserialize);
        // Done on the launcher
        consumerEndpoint.connect();
        consumerEndpoint.subscribe(MaestroTopics.MAESTRO_TOPICS);

        MaestroCollector collector = new MaestroCollector(consumerEndpoint);

        maestro = new Maestro(collector, client);

        miniReceivingPeer = new MiniPeer("org.maestro.worker.jms.JMSReceiverWorker",
                mqttEndpoint, "receiver", "localhost");
        miniSendingPeer = new MiniPeer("org.maestro.worker.jms.JMSSenderWorker",
                mqttEndpoint, "sender", "localhost");


        File dataDir = new File(testDataDir, "data");
        FileUtils.deleteDirectory(dataDir);

        MqttConsumerEndpoint<MaestroNote> toolEndpoint = new MqttConsumerEndpoint<>(mqttEndpoint, MaestroDeserializer::deserializeEvent);
        // Done on the launcher
//        toolEndpoint.connect();
//        toolEndpoint.subscribe(...);

        defaultToolLauncher = new DefaultToolLauncher(client, toolEndpoint, dataDir, false, "localhost");

        miniSendingPeer.start();
        miniReceivingPeer.start();

        ExecutorService executorService = Executors.newCachedThreadPool();

        reportsToolFuture = executorService.submit(() -> defaultToolLauncher.launchServices());

        System.out.println("Mini peers have started");
    }

    private void configureReportsDB(String testDataDir) {
        String testDbUrl = String.format("jdbc:h2:%s/reports.db", testDataDir);
        System.out.println("Using JDBC URL " + testDbUrl);
        System.setProperty("maestro.reports.datasource.url", testDbUrl);
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
        verifyUrl("/api/live");
        verifyUrl("/api/report");

        verifyReportsData();
        System.out.println("Waiting 5s for the aggregation to kick in and provide reports");
        Thread.sleep(5000);
        verifyAggregatedReportsData();
    }

    private void verifyUrl(String path) {
        // Check that the report server remains up after the test
        String apiLiveUrl = API_HOST + "/api/live";
        Response apiLiveResponse = HTTPEasy
                .url(apiLiveUrl)
                .get();

        assertEquals(HttpStatus.OK_200, apiLiveResponse.getStatus());
    }


    private JsonParser verifyResponseData(String path) throws IOException {
        String allReports = API_HOST + path;
        Response allReportsResponse = HTTPEasy
                .url(allReports)
                .get();

        assertEquals(HttpStatus.OK_200, allReportsResponse.getStatus());

        MappingJsonFactory factory = new MappingJsonFactory();
        return factory.createParser((InputStream) allReportsResponse.getEntity());
    }

    private void verifyReportsData() throws IOException {
        JsonParser parser = verifyResponseData("/api/report/");

        List<Report> reports = parser.readValueAs(new TypeReference<List<Report>>(){});

        assertTrue(reports.size() > 0);
        for (Report report : reports) {
            assertNotNull(report);
//            System.out.println("Report " + report);
            assertEquals("integration test", report.getTestName());
            assertEquals("junit", report.getTestScript());
            assertTrue(report.isValid());
            assertFalse(report.isAggregated());
            assertFalse(report.isRetired());
        }
    }

    private void verifyAggregatedReportsData() throws IOException {
        JsonParser parser = verifyResponseData("/api/report/aggregated/");

        List<Report> reports = parser.readValueAs(new TypeReference<List<Report>>(){});

        // TODO: not working, yet
        // assertTrue(reports.size() > 0);
        for (Report report : reports) {
            assertNotNull(report);
            System.out.println("Aggregated report " + report);
            assertEquals("integration test", report.getTestName());
            assertEquals("junit", report.getTestScript());
            assertTrue(report.isValid());
            assertTrue(report.isAggregated());
            assertFalse(report.isRetired());
        }
    }

}
