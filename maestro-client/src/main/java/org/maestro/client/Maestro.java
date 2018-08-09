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

package org.maestro.client;

import org.maestro.client.exchange.*;
import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.MaestroRequester;
import org.maestro.common.client.notes.GetOption;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;


/**
 * A maestro instance
 */
public final class Maestro implements MaestroRequester {
    private static final Logger logger = LoggerFactory.getLogger(Maestro.class);

    private final MaestroClient maestroClient;
    private final MaestroCollectorExecutor collectorExecutor;
    private final Thread collectorThread;

    private CountDownLatch startSignal = new CountDownLatch(1);

    /**
     * Constructor
     * @param url URL of the maestro broker
     * @throws MaestroException if unable to connect to the maestro broker
     */
    public Maestro(final String url) throws MaestroException {
        collectorExecutor = new MaestroCollectorExecutor(url);

        maestroClient = new MaestroMqttClient(url);
        maestroClient.connect();

        collectorThread = new Thread(collectorExecutor);
        collectorThread.start();
    }

    /**
     * Stops maestro
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void stop() throws MaestroConnectionException {
        logger.debug("Thread {} is stopping Maestro execution", Thread.currentThread());

        try {
            collectorExecutor.stop();
            collectorThread.join();
        } catch (InterruptedException e) {
            logger.trace("Interrupted while stopping Maestro {}", e.getMessage(), e);
        }
        finally {
            logger.info("Disconnecting the Maestro client");
            maestroClient.disconnect();
        }
    }

    /**
     * Sends a flush request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> flushRequest() throws MaestroConnectionException {
        return flushRequest(MaestroTopics.ALL_DAEMONS);
    }


    /**
     * Sends a flush request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> flushRequest(final String topic) throws MaestroConnectionException {
        FlushRequest maestroNote = new FlushRequest();

        maestroClient.publish(topic, maestroNote);
        return getOkErrorCompletableFuture();
    }

    /**
     * Sends a ping request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> pingRequest() throws MaestroConnectionException {
        return pingRequest(MaestroTopics.ALL_DAEMONS);
    }


    /**
     * Sends a ping request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> pingRequest(final String topic) throws MaestroConnectionException {
        PingRequest maestroNote = new PingRequest();

        maestroClient.publish(topic, maestroNote);

        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000L, note -> note instanceof PingResponse)
        );
    }

    private boolean isSetReply(MaestroNote note) {
        return note instanceof OkResponse || note instanceof InternalError;
    }

    private CompletableFuture<List<? extends MaestroNote>> getSetCompletableFuture() {
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000L, Maestro.this::isSetReply)
        );
    }

    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setBroker(final String value) throws MaestroConnectionException {
        return setBroker(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set broker request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setBroker(final String topic, final String value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setBroker(value);

        maestroClient.publish(topic, maestroNote);
        return getSetCompletableFuture();
    }




    /**
     * Sends a set duration request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setDuration(final Object value) throws MaestroException {
        return setDuration(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set duration request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setDuration(final String topic, final Object value) throws MaestroException {
        SetRequest maestroNote = new SetRequest();

        if (value instanceof String) {
            maestroNote.setDurationType((String) value);
        }
        else {
            if (Long.class.isInstance(value)) {
                maestroNote.setDurationType(Long.toString((long) value));
            }
            else {
                throw new MaestroException("Invalid duration type class " + value.getClass());
            }
        }

        maestroClient.publish(topic, maestroNote);
        return getSetCompletableFuture();
    }



    /**
     * Sends a set log level request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setLogLevel(final String value) throws MaestroConnectionException {
        return setLogLevel(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set log level request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setLogLevel(final String topic, final String value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setLogLevel(value);

        maestroClient.publish(topic, maestroNote);
        return getSetCompletableFuture();
    }


    /**
     * Sends a set parallel count request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setParallelCount(final int value) throws MaestroConnectionException {
        return setParallelCount(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set parallel count request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setParallelCount(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();


        maestroNote.setParallelCount(Integer.toString(value));

        maestroClient.publish(topic, maestroNote);
        return getSetCompletableFuture();
    }

    /**
     * Sends a set message size request (this one can be used for fixed or variable message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setMessageSize(final String value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setMessageSize(value);

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
        return getSetCompletableFuture();
    }


    /**
     * Sends a set message size request (this one can be used for fixed message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setMessageSize(final long value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setMessageSize(Long.toString(value));

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
        return getSetCompletableFuture();
    }


    /**
     * Sends a set throttle request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setThrottle(final int value) throws MaestroConnectionException {
        return setThrottle(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set throttle request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setThrottle(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setThrottle(Integer.toString(value));

        maestroClient.publish(topic, maestroNote);
        return getSetCompletableFuture();
    }


    /**
     * Sends a set rate request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setRate(final int value) throws MaestroConnectionException {
        return setRate(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set rate request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setRate(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setRate(Integer.toString(value));

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
        return getSetCompletableFuture();
    }


    /**
     * Sends a set fail-condition-latency (FCL) request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setFCL(final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setFCL(Integer.toString(value));

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
        return getSetCompletableFuture();
    }


    /**
     * Sets the management interface URL
     * @param value The management interface URL
     * @throws MaestroException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> setManagementInterface(final String value) throws MaestroException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setManagementInterface(value);

        maestroClient.publish(MaestroTopics.INSPECTOR_DAEMONS, maestroNote);
        return getSetCompletableFuture();
    }


    /**
     * Sends a start inspector request
     * @param value The name of the inspector to start. The URL is set via setManagementInterface
     *              command
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> startInspector(final String value) throws MaestroConnectionException {
        StartInspector maestroNote = new StartInspector();

        maestroNote.set(value);

        maestroClient.publish(MaestroTopics.INSPECTOR_DAEMONS, maestroNote);
        return getSetCompletableFuture();
    }


    private Predicate<MaestroNote> isOkOrErrorResponse() {
        return note -> note instanceof OkResponse || note instanceof InternalError;
    }

    private CompletableFuture<List<? extends MaestroNote>> getOkErrorCompletableFuture() {
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, isOkOrErrorResponse())
        );
    }

    /**
     * Sends a stop inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> stopInspector() throws MaestroConnectionException {
        StopInspector maestroNote = new StopInspector();

        maestroClient.publish(MaestroTopics.INSPECTOR_DAEMONS, maestroNote);
        return getOkErrorCompletableFuture();
    }


    /**
     * Sends a start sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> startSender() throws MaestroConnectionException {
        StartSender maestroNote = new StartSender();

        maestroClient.publish(MaestroTopics.SENDER_DAEMONS, maestroNote);
        return getOkErrorCompletableFuture();
    }


    /**
     * Sends a stop sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> stopSender() throws MaestroConnectionException {
        StopSender maestroNote = new StopSender();

        maestroClient.publish(MaestroTopics.SENDER_DAEMONS, maestroNote);
        return getOkErrorCompletableFuture();
    }

    /**
     * Sends a start receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> startReceiver() throws MaestroConnectionException {
        StartReceiver maestroNote = new StartReceiver();

        maestroClient.publish(MaestroTopics.RECEIVER_DAEMONS, maestroNote);
        return getOkErrorCompletableFuture();
    }


    /**
     * Sends a stop receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> stopReceiver() throws MaestroConnectionException {
        StopReceiver maestroNote = new StopReceiver();

        maestroClient.publish(MaestroTopics.RECEIVER_DAEMONS, maestroNote);
        return getOkErrorCompletableFuture();
    }


    /**
     * Sends a stop receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> stopAll() throws MaestroConnectionException {
        StopSender stopSender = new StopSender();

        maestroClient.publish(MaestroTopics.SENDER_DAEMONS, stopSender);

        StopReceiver stopReceiver = new StopReceiver();

        maestroClient.publish(MaestroTopics.RECEIVER_DAEMONS, stopReceiver);

        StopInspector stopInspector = new StopInspector();

        maestroClient.publish(MaestroTopics.INSPECTOR_DAEMONS, stopInspector);

        StopAgent stopAgent = new StopAgent();

        maestroClient.publish(MaestroTopics.AGENT_DAEMONS, stopAgent);

        return getOkErrorCompletableFuture();
    }


    /**
     * Sends a stats request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> statsRequest() throws MaestroConnectionException {
        StatsRequest maestroNote = new StatsRequest();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, note -> note instanceof InternalError || note instanceof StatsResponse)
        );
    }


    /**
     * Sends a halt request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> halt() throws MaestroConnectionException {
        Halt maestroNote = new Halt();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
        return getOkErrorCompletableFuture();
    }


    /**
     * Sends a get request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> getDataServer() throws MaestroConnectionException {
        GetRequest maestroNote = new GetRequest();

        maestroNote.setGetOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, note -> note instanceof InternalError || note instanceof GetResponse)
        );
    }

    /**
     * Sends a start agent request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> startAgent() throws MaestroConnectionException {
        StartAgent maestroNote = new StartAgent();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
        return getOkErrorCompletableFuture();
    }

    /**
     * Sends a stop agent request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> stopAgent() throws MaestroConnectionException {
        StopAgent maestroNote = new StopAgent();

        maestroClient.publish(MaestroTopics.AGENT_DAEMONS, maestroNote);
        return getOkErrorCompletableFuture();
    }

    /**
     * Sends a user command request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> userCommand(final long option, final String payload) throws MaestroConnectionException {
        UserCommand1Request maestroNote = new UserCommand1Request();

        maestroNote.set(option, payload);


        maestroClient.publish(MaestroTopics.AGENT_DAEMONS, maestroNote);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, note -> note instanceof UserCommand1Response || note instanceof InternalError)
        );
    }

    /**
     * Sends a source request to the agent (which causes it to download the given source)
     * @param sourceUrl the source url (ie.: git://host/path/to/extension-endpoint.git)
     * @param branch branch to use for the source URL
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public CompletableFuture<List<? extends MaestroNote>> sourceRequest(final String sourceUrl, final String branch) throws MaestroConnectionException {
        AgentSourceRequest maestroNote = new AgentSourceRequest();

        maestroNote.setSourceUrl(sourceUrl);
        maestroNote.setBranch(branch);

        maestroClient.publish(MaestroTopics.AGENT_DAEMONS, maestroNote);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, isOkOrErrorResponse())
        );
    }

    /**
     * Sends a log request
     * @param locationType The location type
     * @param typeName The optional type name (mandatory if the location type is ANY)
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void logRequest(final LocationType locationType, final String typeName) throws MaestroConnectionException {
        logRequest(MaestroTopics.ALL_DAEMONS, locationType, typeName);
    }


    /**
     * Sends a log request
     * @param topic the topic to send the request to
     * @param locationType The location type
     * @param typeName The optional type name (mandatory if the location type is ANY)
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void logRequest(final String topic, final LocationType locationType, final String typeName) throws MaestroConnectionException {
        LogRequest maestroNote = new LogRequest();

        maestroNote.setLocationType(locationType);
        if (typeName != null) {
            maestroNote.setTypeName(typeName);
        }

        maestroClient.publish(topic, maestroNote);
    }

    public CompletableFuture<List<? extends MaestroNote>> drainRequest(final String topic, final String duration, final String url, int parallelCount) {
        DrainRequest drainRequest = new DrainRequest();

        drainRequest.setDuration(duration);
        drainRequest.setUrl(url);
        drainRequest.setParallelCount(String.valueOf(parallelCount));

        maestroClient.publish(topic, drainRequest);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000,isOkOrErrorResponse())
        );
    }

    /**
     * Collect replies
     */
    public void clear() {
        collectorExecutor.clear();
    }

    private boolean hasReplies(List<?> replies) {
        return (replies != null && replies.size() > 0);
    }


    /**
     * Collect replies up to a certain limit of retries/timeout
     * @param expect The number of replies to expect.
     * @param predicate Returns only the messages matching the predicate
     * @return A list of serialized maestro replies or null if none. May return less that expected.
     */
    private List<MaestroNote> collect(int expect, Predicate<? super MaestroNote> predicate) {
        List<MaestroNote> replies = new LinkedList<>();
        MaestroMonitor monitor = new MaestroMonitor();

        try {
            collectorExecutor.getCollector().monitor(monitor);

            do {
                replies.addAll(collectorExecutor.getCollector().collect(predicate));

                if (hasReplies(replies) && replies.size() >= expect) {
                    break;
                }

                try {
                    synchronized (monitor) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Not enough responses ... waiting");
                        }
                        monitor.wait();
                    }
                } catch (InterruptedException e) {

                }
            } while (replies.size() >= expect);
        }
        finally {
            collectorExecutor.getCollector().remove(monitor);
        }

        return replies;
    }

    /**
     * Collect replies up to a certain limit of retries/timeout
     * @param wait how much time between each retry
     * @param predicate Returns only the messages matching the predicate
     * @return A list of serialized maestro replies or null if none. May return less that expected.
     */
    private List<MaestroNote> collectWithDelay(long wait, Predicate<? super MaestroNote> predicate) {
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            logger.trace("Interrupted while collecting Maestro replies {}", e.getMessage(), e);
        }

        return collectorExecutor.getCollector().collect(predicate);
    }


    /**
     * Get the collector receiving the messages
     * @return the collector receiving the messages
     */
    public MaestroCollector getCollector() {
        return collectorExecutor.getCollector();
    }

    /**
     * Waits for the drain notifications
     * @param expect Number of retries before considering stale (every retry == 1 second of wait)
     * @return A completable future
     */
    public CompletableFuture<List<? extends MaestroNote>> waitForDrain(int expect) {
        return CompletableFuture.supplyAsync(
                () -> collect(expect,
                        note -> note instanceof DrainCompleteNotification || note instanceof InternalError)
        );
    }

    /**
     * Waits for the drain notifications
     * @return A completable future
     */
    public CompletableFuture<List<? extends MaestroNote>> waitForDrain() {
        return waitForDrain(1);
    }


    public CompletableFuture<List<? extends MaestroNote>> waitForNotifications(int expect) {
        return CompletableFuture.supplyAsync(
                () -> collect(expect, maestroNotificationPredicate())
        );
    }

    private Predicate<MaestroNote> maestroNotificationPredicate() {
        return note -> note instanceof TestSuccessfulNotification || note instanceof InternalError || note instanceof TestFailedNotification;
    }

}
