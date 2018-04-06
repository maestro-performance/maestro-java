/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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

package org.maestro.worker.base;

import org.maestro.client.exchange.AbstractMaestroPeer;
import org.maestro.client.notes.*;
import org.maestro.common.URLQuery;
import org.maestro.common.client.notes.GetOption;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.test.TestProperties;
import org.maestro.common.worker.*;
import org.maestro.client.MaestroReceiverClient;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.worker.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * A base worker class that implements the most basic worker functionality
 */
public abstract class MaestroWorkerManager extends AbstractMaestroPeer<MaestroEvent> implements MaestroEventListener {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerManager.class);

    private final MaestroReceiverClient client;
    private WorkerOptions workerOptions;
    private boolean running = true;
    private MaestroDataServer dataServer;

    /**
     * Constructor
     * @param maestroURL Maestro broker URL
     * @param role Worker role
     * @param host hostname
     * @throws MaestroException
     */
    public MaestroWorkerManager(final String maestroURL, final String role, final String host, final MaestroDataServer dataServer) throws MaestroException {
        super(maestroURL, role, MaestroDeserializer::deserializeEvent);

        logger.debug("Creating the receiver client");
        client = new MaestroReceiverClient(maestroURL, clientName, host, id);

        workerOptions = new WorkerOptions();
        this.dataServer = dataServer;
    }


    protected WorkerOptions getWorkerOptions() {
        return workerOptions;
    }


    protected void setWorkerOptions(WorkerOptions workerOptions) {
        this.workerOptions = workerOptions;
    }


    protected MaestroReceiverClient getClient() {
        return client;
    }


    @Override
    public void connect() throws MaestroConnectionException {
        super.connect();

        client.connect();
    }


    protected void setRunning(boolean running) {
        this.running = running;
    }


    @Override
    public boolean isRunning() {
        return running;
    }


    @Override
    protected final void noteArrived(MaestroEvent note) throws MaestroConnectionException {
        logger.debug("Some message arrived: {}", note.toString());
        note.notify(this);
    }


    @Override
    public void handle(final StatsRequest note) {
        logger.debug("Stats request received");
        StatsResponse statsResponse = new StatsResponse();

        String parallelCount = workerOptions.getParallelCount();

        if (parallelCount == null) {
            statsResponse.setChildCount(0);
        }
        else {
            statsResponse.setChildCount(Integer.parseInt(parallelCount));
        }

        // Explanation: the role is the name as the role (ie: clientName@host)
        statsResponse.setRole(getClientName());
        statsResponse.setLatency(0);
        statsResponse.setRate(0);
        statsResponse.setRoleInfo("");
        statsResponse.setTimestamp("0");

        client.statsResponse(statsResponse);
    }


    @Override
    public void handle(FlushRequest note) {
        logger.debug("Flush request received");
    }


    @Override
    public void handle(Halt note) {
        logger.debug("Halt request received");

        setRunning(false);
    }


    @Override
    public void handle(SetRequest note) {
        logger.debug("Set request received");

        switch (note.getOption()) {
            case MAESTRO_NOTE_OPT_SET_BROKER: {
                workerOptions.setBrokerURL(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_DURATION_TYPE: {
                workerOptions.setDuration(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_LOG_LEVEL: {
                workerOptions.setLogLevel(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_PARALLEL_COUNT: {
                workerOptions.setParallelCount(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_MESSAGE_SIZE: {
                workerOptions.setMessageSize(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_THROTTLE: {
                workerOptions.setThrottle(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_RATE: {
                workerOptions.setRate(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_FCL: {
                workerOptions.setFcl(note.getValue());
            }
        }
    }


    protected void writeTestProperties(final File testLogDir) throws IOException, URISyntaxException, DurationParseException {
        TestProperties testProperties = new TestProperties();

        testProperties.setBrokerUri(workerOptions.getBrokerURL());

        testProperties.setDuration(workerOptions.getDuration());

        testProperties.setParallelCount(workerOptions.getParallelCount());

        // Note: it already sets the variable size flag for variable message sizes
        testProperties.setMessageSize(workerOptions.getMessageSize());

        testProperties.setRate(workerOptions.getRate());
        testProperties.setFcl(workerOptions.getFcl());

        final URLQuery urlQuery = new URLQuery(workerOptions.getBrokerURL());

        testProperties.setProtocol(urlQuery.getString("protocol", "AMQP"));
        testProperties.setLimitDestinations(urlQuery.getInteger("limitDestinations", 1));

        // TODO: collect this
        testProperties.setApiName("JMS");
        testProperties.setApiVersion("1.1");

        testProperties.write(new File(testLogDir, "test.properties"));
    }

    @Override
    public void handle(TestFailedNotification note) {
        logger.info("Test failed notification received from {}: {}", note.getName(), note.getMessage());
    }

    @Override
    public void handle(TestSuccessfulNotification note) {
        logger.info("Test successful notification received from {}: {}", note.getName(), note.getMessage());
    }

    @Override
    public void handle(AbnormalDisconnect note) {
        logger.info("Abnormal disconnect notification received from {}: {}", note.getName(), note.getMessage());
    }

    @Override
    public void handle(PingRequest note) throws MaestroConnectionException, MalformedNoteException {
        client.pingResponse(note.getSec(), note.getUsec());
    }


    @Override
    public void handle(GetRequest note) {
        logger.info("A get request has arrived");
        switch (note.getOption()) {
            case MAESTRO_NOTE_OPT_GET_DS: {
                String dataServerAddress = dataServer.getServerURL();

                GetResponse response = new GetResponse();

                response.setOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);
                response.setValue(dataServerAddress);

                client.getResponse(response);
            }
        }

    }
}
