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

package org.maestro.worker.common;

import org.maestro.client.MaestroReceiverClient;
import org.maestro.client.exchange.AbstractMaestroPeer;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.GroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.Role;
import org.maestro.common.URLQuery;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.GetOption;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.test.TestProperties;
import org.maestro.common.test.SystemProperties;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.contrib.utils.digest.Sha1Digest;
import org.maestro.worker.common.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * A base worker class that implements the most basic worker functionality
 */
public abstract class MaestroWorkerManager extends AbstractMaestroPeer<MaestroEvent<MaestroEventListener>> implements MaestroEventListener {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerManager.class);

    private final MaestroReceiverClient client;
    private final WorkerOptions workerOptions;
    private boolean running = true;
    private final MaestroDataServer dataServer;
    private GroupInfo groupInfo;

    /**
     * Constructor
     * @param maestroURL Maestro broker URL
     * @param peerInfo Information about this peer
     * @param dataServer the data server instance
     */
    public MaestroWorkerManager(final String maestroURL, final PeerInfo peerInfo, final MaestroDataServer dataServer) {
        super(maestroURL, peerInfo, MaestroDeserializer::deserializeEvent);

        logger.debug("Creating the receiver client");
        client = new MaestroReceiverClient(maestroURL, peerInfo, getId());

        workerOptions = new WorkerOptions();
        this.dataServer = dataServer;
    }


    protected WorkerOptions getWorkerOptions() {
        return workerOptions;
    }


    protected MaestroReceiverClient getClient() {
        return client;
    }


    @Override
    public void connect() throws MaestroConnectionException {
        super.connect();

        client.connect();
    }


    void setRunning(boolean running) {
        this.running = running;
    }


    @Override
    public boolean isRunning() {
        return running;
    }


    @Override
    protected final void noteArrived(MaestroEvent<MaestroEventListener> note) throws MaestroConnectionException {
        if (logger.isTraceEnabled()) {
            logger.trace("Some message arrived: {}", note.toString());
        }

        note.notify(this);
    }


    @Override
    public void handle(final StatsRequest note) {

        if (logger.isTraceEnabled()) {
            logger.trace("Stats request received");
        }

        StatsResponse statsResponse = new StatsResponse();

        String parallelCount = workerOptions.getParallelCount();

        if (parallelCount == null) {
            statsResponse.setChildCount(0);
        }
        else {
            statsResponse.setChildCount(Integer.parseInt(parallelCount));
        }

        statsResponse.setPeerInfo(getPeerInfo());
        statsResponse.setLatency(0);
        statsResponse.setRate(0);
        statsResponse.setRoleInfo("");
        statsResponse.setTimestamp("0");

        statsResponse.correlate(note);

        client.statsResponse(statsResponse);
    }

    @Override
    public void handle(Halt note) {
        logger.trace("Halt request received");

        setRunning(false);
    }


    @Override
    public void handle(SetRequest note) {
        logger.trace("Set request received");

        switch (note.getOption()) {
            case MAESTRO_NOTE_OPT_SET_BROKER: {
                workerOptions.setBrokerURL(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_DURATION_TYPE: {
                workerOptions.setDuration(note.getValue());
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
            case MAESTRO_NOTE_OPT_SET_RATE: {
                workerOptions.setRate(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_FCL: {
                workerOptions.setFcl(note.getValue());
                break;
            }
        }
    }


    void writeTestProperties(final File testLogDir) throws IOException, DurationParseException {
        TestProperties testProperties = new TestProperties();

        final String testNumber = testLogDir.getName();

        final String brokerURL = workerOptions.getBrokerURL();
        logger.info("Broker URL for test {}: {}", testNumber, brokerURL);
        testProperties.setBrokerUri(brokerURL);

        final String duration = workerOptions.getDuration();
        logger.info("Test duration for test {}: {}", testNumber, duration);
        testProperties.setDuration(duration);

        final String parallelCount = workerOptions.getParallelCount();
        logger.info("Parallel count for test {}: {}", testNumber, parallelCount);
        testProperties.setParallelCount(parallelCount);

        // Note: it already sets the variable size flag for variable message sizes
        final String messageSize = workerOptions.getMessageSize();
        logger.info("Message size for test {}: {}", testNumber, messageSize);
        testProperties.setMessageSize(messageSize);

        final String rate = workerOptions.getRate();
        logger.info("Target rate for test {}: {}", testNumber, rate);
        testProperties.setRate(rate);

        final String fcl = workerOptions.getFcl();
        logger.info("FCL for test {}: {}", testNumber, fcl);
        testProperties.setFcl(fcl);

        final URLQuery urlQuery;
        try {
            urlQuery = new URLQuery(brokerURL);

            testProperties.setProtocol(urlQuery.getString("protocol", "AMQP"));
            testProperties.setLimitDestinations(urlQuery.getInteger("limitDestinations", 1));
        } catch (URISyntaxException e) {
            logger.warn("The URL provided by the front-end is invalid/non-parseable");

            testProperties.setProtocol("undefined");
            testProperties.setLimitDestinations(0);
        }

        // TODO: collect this
        testProperties.setApiName("JMS");
        testProperties.setApiVersion("1.1");

        testProperties.write(new File(testLogDir, TestProperties.FILENAME));
    }

    /***
     * Method for write system properties into the file for the reporter.
     * @param testLogDir test log directory
     * @throws IOException Input/Output exception
     */
    protected void writeSystemProperties(final File testLogDir) throws  IOException {
        SystemProperties systemProperties = new SystemProperties();

        Runtime runtime = Runtime.getRuntime();

        systemProperties.setWorkerSystemCpuCount(runtime.availableProcessors());
        logger.info("System CPU count: {}", systemProperties.getWorkerSystemCpuCount());

        systemProperties.setWorkerJVMMaxMemory(runtime.maxMemory());
        logger.info("JVM Max Memory: {}", systemProperties.getWorkerSystemMemory());

        systemProperties.setWorkerOperatingSystemName(System.getProperty("os.name"));
        logger.info("System Name: {}", systemProperties.getWorkerOperatingSystemName());

        systemProperties.setWorkerOperatingSystemArch(System.getProperty("os.arch"));
        logger.info("System Arch: {}", systemProperties.getWorkerOperatingSystemArch());

        systemProperties.setWorkerOperatingSystemVersion(System.getProperty("os.version"));
        logger.info("System Version: {}", systemProperties.getWorkerOperatingSystemVersion());

        systemProperties.setWorkerJavaVersion(System.getProperty("java.version"));
        logger.info("Java Version: {}", systemProperties.getWorkerJavaVersion());

        systemProperties.setWorkerJavaHome(System.getProperty("java.home"));
        logger.info("Java Home: {}", systemProperties.getWorkerJavaHome());

        systemProperties.setWorkerJvmName(System.getProperty("java.vm.name"));
        logger.info("JVM Name: {}", systemProperties.getWorkerJvmName());

        systemProperties.setWorkerJvmVersion(System.getProperty("java.vm.version"));
        logger.info("JVM Version: {}", systemProperties.getWorkerJvmVersion());

        logger.info(testLogDir.toString());

        systemProperties.write(new File(testLogDir, SystemProperties.FILENAME));
    }

    @Override
    public void handle(TestFailedNotification note) {
        logger.info("Test failed notification received from {}: {}", note.getPeerInfo().prettyName(), note.getMessage());
    }

    @Override
    public void handle(TestSuccessfulNotification note) {
        logger.info("Test successful notification received from {}: {}", note.getPeerInfo().prettyName(), note.getMessage());
    }

    @Override
    public void handle(AbnormalDisconnect note) {
        logger.info("Abnormal disconnect notification received from {}: {}", note.getPeerInfo().prettyName(), note.getMessage());
    }

    @Override
    public void handle(PingRequest note) throws MaestroConnectionException, MalformedNoteException {
        client.pingResponse(note, note.getSec(), note.getUsec());
    }


    @Override
    public void handle(GetRequest note) {
        logger.trace("A get request has arrived");
        switch (note.getOption()) {
            case MAESTRO_NOTE_OPT_GET_DS: {
                String dataServerAddress = dataServer.getServerURL();

                GetResponse response = new GetResponse();

                response.setOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);
                response.setValue(dataServerAddress);
                response.correlate(note);

                client.getResponse(response);
            }
        }
    }

    @Override
    public void handle(DrainCompleteNotification note) {
        // NO-OP
    }

    /**
     * Handle a log request note
     * @param note the note to handle
     * @param logDir the log directory
     */
    protected void handle(final LogRequest note, final File logDir) {
        logger.debug("Log request received");
        File logSubDir;

        switch (note.getLocationType()) {
            case LAST_FAILED: {
                logSubDir = TestLogUtils.lastFailedTestLogDir(logDir);
                break;
            }
            case LAST_SUCCESS: {
                logSubDir = TestLogUtils.lastSuccessfulTestLogDir(logDir);
                break;
            }

            case ANY: {
                String name = note.getTypeName();
                logSubDir = TestLogUtils.anyTestLogDir(logDir, name);
                break;
            }
            case LAST:
            default: {
                logSubDir = TestLogUtils.lastTestLogDir(logDir);
                break;
            }
        }

        if (!logSubDir.exists()) {
            logger.error("The client requested the log files for location {} but they don't exist at {}",
                    note.getLocationType().toString(), logDir);

            getClient().replyInternalError(note,"The client requested the log files for location %s but they don't exist at %s",
                    note.getLocationType().toString(), logDir.getPath());
            return;
        }

        File[] files = logSubDir.listFiles();
        if (files == null) {
            logger.error("The client request log files, but the location does not contain any");

            getClient().replyInternalError(note,"The client requested the log files for location %s but there's no files there",
                    note.getLocationType().toString());

            return;
        }

        for (File file : files) {
            logger.debug("Sending log file {} with location type {}", file.getName(),
                    note.getLocationType());

            Sha1Digest digest = new Sha1Digest();

            String hash;
            try {
                hash = digest.calculate(file);
            } catch (IOException e) {
                logger.error("Unable to calculate hash for file {}", file.getName());
                hash = "";
            }

            getClient().logResponse(file, note.getLocationType(), hash);
        }
    }


    @Override
    public void handle(final GroupJoinRequest note) {
        GroupInfo requested = note.getGroupInfo();

        if (groupInfo != null) {
            getClient().replyInternalError(note, "Cannot join group %s as %s: the worker is already part of the worker group %s as %s",
                requested.groupName(), requested.memberName(), groupInfo.groupName(), groupInfo.memberName());

            return;
        }

        final String topicName = MaestroTopics.peerTopic(requested);
        getClient().subscribe(topicName, 0);
        this.groupInfo = requested;

        logger.info("Successfully joined group {} as {}", groupInfo.groupName(), groupInfo.memberName());
        getClient().replyOk(note);
    }

    @Override
    public void handle(final GroupLeaveRequest note) {
        if (groupInfo == null) {
            logger.warn("Ignoring a group leave request because not a member of any group");

            getClient().replyOk(note);

            return;
        }

        final String peerTopics = MaestroTopics.peerTopic(groupInfo);
        getClient().unsubscribe(peerTopics);

        final  GroupInfo old = groupInfo;
        groupInfo = null;
        getClient().replyOk(note);

        logger.info("Successfully left group {} as {}", old.groupName(), old.memberName());
    }

    @Override
    public void handle(final RoleAssign note) {
        Role role = note.getRole();

        if (getPeerInfo().getRole() != Role.OTHER) {
            getClient().replyInternalError(note, "The node is already assigned the %s role",
                    getPeerInfo().getRole().toString());

            return;
        }

        String roleTopic = MaestroTopics.peerTopic(role);
        getClient().subscribe(roleTopic, 0);
        logger.debug("Subscribed to the role topic at {}", roleTopic);

        logger.info("The worker was assigned the {} role", role);
        getPeerInfo().setRole(role);
        getClient().replyOk(note);
    }

    @Override
    public void handle(final RoleUnassign note) {
        if (getPeerInfo().getRole() != Role.OTHER) {
            String roleTopic = MaestroTopics.peerTopic(getPeerInfo().getRole());
            getClient().unsubscribe(roleTopic);
            logger.debug("Unsubscribed to the role topic at {}", roleTopic);
        }

        getPeerInfo().setRole(Role.OTHER);
        getClient().replyOk(note);
    }

    @Override
    abstract public void handle(final LogRequest note);

    @Override
    public void handle(final StartTestRequest note) {
        // NO-OP
    }

    @Override
    public void handle(final StartTestNotification note) {
        // NO-OP
    }

    @Override
    public void handle(final StopTestRequest note) {
        // NO-OP
    }
}
