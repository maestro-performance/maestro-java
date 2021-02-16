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

import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.client.exchange.MaestroNoteDeserializer;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.peer.AbstractMaestroPeer;
import org.maestro.client.exchange.receiver.MaestroReceiverClient;
import org.maestro.client.exchange.support.GroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.Role;
import org.maestro.common.URLQuery;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.ServiceLevel;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.LocationTypeInfo;
import org.maestro.common.client.notes.Test;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.test.TestProperties;
import org.maestro.common.test.SystemProperties;
import org.maestro.common.test.properties.PropertyWriter;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.contrib.utils.digest.Sha1Digest;
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
    private volatile boolean running = true;
    private GroupInfo groupInfo;
    private Test currentTest;

    /**
     * Constructor
     * @param maestroClient Maestro client
     * @param consumerEndpoint consumer endpoint
     * @param peerInfo Information about this peer
     */
    public MaestroWorkerManager(MaestroClient maestroClient, ConsumerEndpoint consumerEndpoint, PeerInfo peerInfo) {
        this(maestroClient, consumerEndpoint, peerInfo, MaestroDeserializer::deserializeEvent);
    }

    /**
     * Constructor
     * @param maestroClient Maestro client
     * @param peerInfo Information about this peer
     * @param deserializer the deserializer to use
     */
    protected MaestroWorkerManager(MaestroClient maestroClient, ConsumerEndpoint consumerEndpoint, PeerInfo peerInfo, MaestroNoteDeserializer<MaestroEvent<MaestroEventListener>> deserializer) {
        super(consumerEndpoint, peerInfo);

        logger.debug("Creating the receiver client");
        client = new MaestroReceiverClient(maestroClient, peerInfo, getId());

        workerOptions = new WorkerOptions();
    }


    protected WorkerOptions getWorkerOptions() {
        return workerOptions;
    }


    protected MaestroReceiverClient getClient() {
        return client;
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
        logger.info("Halt request received");

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


    protected TestProperties getTestProperties(final String testNumber) throws DurationParseException {
        TestProperties testProperties = new TestProperties();

        final String brokerURL = workerOptions.getBrokerURL();
        logger.info("Broker URL for test {}: {}", testNumber, brokerURL);
        testProperties.setBrokerUri(brokerURL);

        final String duration = workerOptions.getDuration();
        logger.info("Test duration for test {}: {}", testNumber, duration);
        testProperties.setDurationFromSpec(duration);

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

        return testProperties;
    }


    protected void writeTestProperties(final File testLogDir) throws IOException, DurationParseException {
        final String testNumber = testLogDir.getName();
        final TestProperties testProperties = getTestProperties(testNumber);

        logger.info("Test properties for test {}: {}", testNumber, testProperties.toString());

        final PropertyWriter writer = new PropertyWriter();
        writer.write(testProperties, new File(testLogDir, TestProperties.FILENAME));
    }


    protected SystemProperties getSystemProperties() {
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

        return systemProperties;
    }


    /***
     * Method for write system properties into the file for the reporter.
     * @param testLogDir test log directory
     * @throws IOException Input/Output exception
     */
    protected void writeSystemProperties(final File testLogDir) throws  IOException {
        final SystemProperties systemProperties = getSystemProperties();

        final PropertyWriter writer = new PropertyWriter();
        writer.write(systemProperties, new File(testLogDir, SystemProperties.FILENAME));
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
                client.replyInternalError(note, "Data server is deprecated");
                break;
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
     * @param peerInfo the peer information
     */
    protected void handle(final LogRequest note, final File logDir, final PeerInfo peerInfo) {
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

        final File[] files = logSubDir.listFiles();
        if (files == null || files.length == 0) {
            logger.error("The client request log files, but the location does not contain any");

            getClient().replyInternalError(note,"The client requested the log files for location %s but there's no files there",
                    note.getLocationType().toString());

            return;
        }

        int index = 0;
        final LocationTypeInfo locationTypeInfo = new LocationTypeInfo(files.length);

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

            locationTypeInfo.setIndex(index);
            MaestroReceiverClient.logResponse(file, note, hash, locationTypeInfo, peerInfo, getId(),
                    (MaestroClient) getClient());
            index++;
        }
    }

    @Deprecated
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

        if (getPeerInfo().getRole() != Role.OTHER ) {
            getClient().replyInternalError(note, "The node is already assigned the %s role",
                    getPeerInfo().getRole().toString());

            return;
        }

        String roleTopic = MaestroTopics.peerTopic(role);
        getClient().subscribe(roleTopic, ServiceLevel.AT_MOST_ONCE);
        logger.debug("Subscribed to the role topic at {}", roleTopic);

        logger.info("The worker was assigned the {} role", role);
        getPeerInfo().setRole(role);
        getClient().replyOk(note);
    }

    @Override
    public void handle(final RoleUnassign note) {
        try {
            if (getPeerInfo().getRole() != Role.OTHER) {
                final String roleTopic = MaestroTopics.peerTopic(getPeerInfo().getRole());
                getClient().unsubscribe(roleTopic);
                logger.debug("Unsubscribed to the role topic at {}", roleTopic);
            }
        } finally {
            getPeerInfo().setRole(Role.OTHER);
        }

        getClient().replyOk(note);
    }

    @Override
    abstract public void handle(final LogRequest note);

    protected void setCurrentTest(final Test test) {
        currentTest = test;
    }

    @Override
    public void handle(final StartTestRequest note) {
        logger.info("Starting a new test: {}", note.getTestExecutionInfo().getTest());

        setCurrentTest(note.getTestExecutionInfo().getTest());
        getClient().replyOk(note);
    }

    @Override
    public void handle(final StopTestRequest note) {
        logger.info("Stopping a test: {}", currentTest);
        currentTest = null;
    }

    protected Test getCurrentTest() {
        return currentTest;
    }

    @Override
    public void handle(final TestStartedNotification note) {
        logger.info("Test started notification received from {} {}", note.getPeerInfo().prettyName(),
                note.getMessage());
    }
}
