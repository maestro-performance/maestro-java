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
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.exchange.support.PeerSet;
import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.maestro.common.NonProgressingStaleChecker;
import org.maestro.common.Role;
import org.maestro.common.StaleChecker;
import org.maestro.common.agent.Source;
import org.maestro.common.agent.UserCommandData;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.MaestroRequester;
import org.maestro.common.client.exceptions.NotEnoughRepliesException;
import org.maestro.common.client.notes.*;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;


/**
 * A maestro instance
 */
public final class Maestro implements MaestroRequester {
    private static final Logger logger = LoggerFactory.getLogger(Maestro.class);

    private final MaestroClient maestroClient;
    private final MaestroCollectorExecutor collectorExecutor;
    private final Thread collectorThread;

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

    @Override
    public CompletableFuture<List<? extends MaestroNote>> pingRequest() throws MaestroConnectionException {
        return pingRequest(MaestroTopics.PEER_TOPIC);
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> pingRequest(final String topic) throws MaestroConnectionException {
        PingRequest maestroNote = new PingRequest();

        maestroClient.publish(topic, maestroNote);

        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }


    private boolean isCorrelated(final MaestroNote note, final MessageCorrelation correlation) {
        return note.correlatesTo(correlation);
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> setBroker(final String topic, final String value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setBroker(value);

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> setDuration(final String topic, final Object value) throws MaestroException {
        SetRequest maestroNote = new SetRequest();

        if (value instanceof String) {
            maestroNote.setDurationType((String) value);
        }
        else {
            if (value instanceof Long) {
                maestroNote.setDurationType(Long.toString((long) value));
            }
            else {
                throw new MaestroException("Invalid duration type class " + value.getClass());
            }
        }

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> setParallelCount(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();


        maestroNote.setParallelCount(Integer.toString(value));

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> setMessageSize(final String topic, final long value) throws MaestroConnectionException {
        return setMessageSize(topic, Long.toString(value));
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> setMessageSize(final String topic, final String value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setMessageSize(value);

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> setRate(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setRate(Integer.toString(value));

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> setFCL(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setFCL(Integer.toString(value));

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> setManagementInterface(final String topic, final String value) throws MaestroException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setManagementInterface(value);

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> startInspector(final String value) throws MaestroConnectionException {
        StartInspector maestroNote = new StartInspector(value);

        maestroClient.publish(MaestroTopics.peerTopic(Role.INSPECTOR), maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }


    private Predicate<MaestroNote> isOkOrErrorResponse() {
        return note -> note instanceof OkResponse || note instanceof InternalError;
    }


    private CompletableFuture<List<? extends MaestroNote>> getOkErrorCompletableFuture() {
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, isOkOrErrorResponse())
        );
    }


    @Deprecated
    public CompletableFuture<List<? extends MaestroNote>> stopInspector() throws MaestroConnectionException {
        StopInspector maestroNote = new StopInspector();

        maestroClient.publish(MaestroTopics.peerTopic(Role.INSPECTOR), maestroNote);
        return getOkErrorCompletableFuture();
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> stopInspector(final String topic) throws MaestroConnectionException {
        StopInspector maestroNote = new StopInspector();

        maestroClient.publish(topic, maestroNote);
        return getOkErrorCompletableFuture();
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> startWorker(final String topic, final WorkerStartOptions options)
            throws MaestroConnectionException {
        StartWorker maestroNote = new StartWorker(options);

        maestroClient.publish(topic, maestroNote);
        return getOkErrorCompletableFuture();
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> stopWorker(final String topic) throws MaestroConnectionException {
        StopWorker maestroNote = new StopWorker();

        maestroClient.publish(topic, maestroNote);
        return getOkErrorCompletableFuture();
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> stopAll() throws MaestroConnectionException {
        StopWorker stopSender = new StopWorker();

        maestroClient.publish(MaestroTopics.WORKERS_TOPIC, stopSender);

        StopInspector stopInspector = new StopInspector();

        maestroClient.publish(MaestroTopics.peerTopic(Role.INSPECTOR), stopInspector);

        StopAgent stopAgent = new StopAgent();

        maestroClient.publish(MaestroTopics.peerTopic(Role.AGENT), stopAgent);

        return getOkErrorCompletableFuture();
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> statsRequest() throws MaestroConnectionException {
        return statsRequest(MaestroTopics.WORKERS_TOPIC);
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> statsRequest(final String topic) throws MaestroConnectionException {
        StatsRequest maestroNote = new StatsRequest();

        maestroClient.publish(topic, maestroNote);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, note -> note instanceof InternalError || note instanceof StatsResponse)
        );
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> halt() throws MaestroConnectionException {
        return halt(MaestroTopics.PEER_TOPIC);
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> halt(final String topic) throws MaestroConnectionException {
        Halt maestroNote = new Halt();

        maestroClient.publish(topic, maestroNote);
        return getOkErrorCompletableFuture();
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> getDataServer() throws MaestroConnectionException {
        GetRequest maestroNote = new GetRequest();

        maestroNote.setGetOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);

        maestroClient.publish(MaestroTopics.PEER_TOPIC, maestroNote);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, note -> note instanceof InternalError || note instanceof GetResponse)
        );
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> startAgent() throws MaestroConnectionException {
        StartAgent maestroNote = new StartAgent();

        maestroClient.publish(MaestroTopics.peerTopic(Role.AGENT), maestroNote);
        return getOkErrorCompletableFuture();
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> stopAgent() throws MaestroConnectionException {
        StopAgent maestroNote = new StopAgent();

        maestroClient.publish(MaestroTopics.peerTopic(Role.AGENT), maestroNote);
        return getOkErrorCompletableFuture();
    }


    @Override
    public CompletableFuture<List<? extends MaestroNote>> userCommand(final String topic, final UserCommandData userCommandData) throws MaestroConnectionException {
        UserCommand1Request maestroNote = new UserCommand1Request();

        maestroNote.set(userCommandData.getOption(), userCommandData.getPayload());


        maestroClient.publish(topic, maestroNote);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, note -> note instanceof UserCommand1Response || note instanceof InternalError)
        );
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> sourceRequest(final String topic, final Source source)
            throws MaestroConnectionException {
        AgentSourceRequest maestroNote = new AgentSourceRequest();

        maestroNote.setSourceUrl(source.getSourceUrl());
        maestroNote.setBranch(source.getBranch());

        maestroClient.publish(topic, maestroNote);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000, isOkOrErrorResponse())
        );
    }

    @Override
    public void logRequest(final String topic, final LocationType locationType, final String typeName) throws MaestroConnectionException {
        LogRequest maestroNote = new LogRequest();

        maestroNote.setLocationType(locationType);
        if (typeName != null) {
            maestroNote.setTypeName(typeName);
        }

        maestroClient.publish(topic, maestroNote);
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> drainRequest(final String topic, final  DrainOptions drainOptions) {
        DrainRequest drainRequest = new DrainRequest();

        drainRequest.setDuration(drainOptions.getDuration());
        drainRequest.setUrl(drainOptions.getUrl());
        drainRequest.setParallelCount(String.valueOf(drainOptions.getParallelCount()));

        maestroClient.publish(topic, drainRequest);
        return CompletableFuture.supplyAsync(
                () -> collectWithDelay(1000,isOkOrErrorResponse())
        );
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> roleAssign(final String topic, final Role role) throws MaestroConnectionException {
        RoleAssign maestroNote = new RoleAssign(role);

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> roleUnassign(final String topic) throws MaestroConnectionException {
        RoleUnassign maestroNote = new RoleUnassign();

        maestroClient.publish(topic, maestroNote);
        MessageCorrelation correlation = maestroNote.correlate();

        return CompletableFuture.supplyAsync(
                () -> collect(note -> isCorrelated(note, correlation))
        );
    }


    /**
     * Clear the container of received messages
     */
    public void clear() {
        collectorExecutor.clear();
    }

    /**
     * Collect replies up to a certain limit of retries/timeout
     * @param expect The number of replies to expect.
     * @param predicate Returns only the messages matching the predicate
     * @return A list of serialized maestro replies or null if none. May return less that expected.
     */
    private List<MaestroNote> collect(int expect, Predicate<? super MaestroNote> predicate) {
        List<MaestroNote> replies = new LinkedList<>();
        MaestroMonitor monitor = new MaestroMonitor(predicate);

        try {
            collectorExecutor.getCollector().monitor(monitor);

            do {
                replies.addAll(collectorExecutor.getCollector().collect(predicate));
                logger.trace("Collected {} of {}", replies.size(), expect);

                if (replies.size() >= expect) {
                    break;
                }

                try {
                    logger.trace("Not enough responses matching the predicate. Waiting for more messages to arrive");
                    monitor.doLock();
                } catch (InterruptedException e) {
                    logger.trace("Interrupted while waiting for message collection");
                }

                logger.trace("Out of the collection lock. Checking for new messages");
            } while (true);

            logger.trace("Exiting the collection loop: {} collected of {} expected for {}", replies.size(), expect,
                    predicate);
        }
        finally {
            collectorExecutor.getCollector().remove(monitor);
        }

        return replies;
    }

    /**
     * Collect replies up to a certain limit of retries/timeout
     * @param predicate Returns only the messages matching the predicate
     * @return A list of serialized maestro replies or null if none. May return less that expected.
     */
    private List<MaestroNote> collect(Predicate<? super MaestroNote> predicate) {
        List<MaestroNote> replies = new LinkedList<>();
        MaestroMonitor monitor = new MaestroMonitor(predicate);
        StaleChecker staleChecker = new NonProgressingStaleChecker(10);

        try {
            collectorExecutor.getCollector().monitor(monitor);

            do {
                replies.addAll(collectorExecutor.getCollector().collect(predicate));
                logger.trace("Collected {} notes", replies.size());

                if (staleChecker.isStale(replies.size())) {
                    break;
                }

                try {
                    logger.trace("Not enough responses matching the predicate. Waiting for more messages to arrive");
                    monitor.doLock(50);
                } catch (InterruptedException e) {
                    logger.trace("Interrupted while waiting for message collection");
                }

                logger.trace("Out of the collection lock. Checking for new messages");
            } while (true);

            logger.trace("Exiting the collection loop: {} collected for {}", replies.size(), predicate);
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

    @Override
    public CompletableFuture<List<? extends MaestroNote>> waitForDrain() {
        return waitForDrain(1);
    }

    @Override
    public CompletableFuture<List<? extends MaestroNote>> waitForNotifications(int expect) {
        return CompletableFuture.supplyAsync(
                () -> collect(expect, maestroNotificationPredicate())
        );
    }

    private Predicate<MaestroNote> maestroNotificationPredicate() {
        return note -> note instanceof TestSuccessfulNotification || note instanceof InternalError || note instanceof TestFailedNotification;
    }


    public static <T> void set(Function<T, CompletableFuture<List<? extends MaestroNote>>> function, T value) {
        final int timeout = 2;

        List<? extends MaestroNote> replies;
        try {
            replies = function.apply(value).get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new MaestroException(e);
        }
        catch (TimeoutException e) {
            throw new NotEnoughRepliesException("Timed out waiting for replies from the test cluster", e);
        }

        if (replies.size() == 0) {
            throw new NotEnoughRepliesException("Not enough replies when trying to execute a command on the test cluster");
        }

        for (MaestroNote reply : replies) {
            if (reply instanceof InternalError) {
                InternalError ie = (InternalError) reply;
                throw new MaestroException("Error applying a setting to the test cluster: %s", ie.getMessage());
            }
        }
    }


    public static <T, U> void set(BiFunction<T, U, CompletableFuture<List<? extends MaestroNote>>> function, T value1, U value2) {
        final int timeout = 2;

        List<? extends MaestroNote> replies;
        try {
            replies = function.apply(value1, value2).get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new MaestroException(e);
        }
        catch (TimeoutException e) {
            throw new NotEnoughRepliesException("Timed out waiting for replies from the test cluster", e);
        }

        if (replies.size() == 0) {
            throw new NotEnoughRepliesException("Not enough replies when trying to execute a command on the test cluster");
        }

        for (MaestroNote reply : replies) {
            if (reply instanceof InternalError) {
                InternalError ie = (InternalError) reply;
                throw new MaestroException("Error applying a setting to the test cluster: %s", ie.getMessage());
            }
        }
    }

    public static <T> void exec(Supplier<CompletableFuture<List<? extends MaestroNote>>> function) {
        final int timeout = 2;

        List<? extends MaestroNote> replies;
        try {
            replies = function.get().get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new MaestroException(e);
        }
        catch (TimeoutException e) {
            throw new NotEnoughRepliesException("Timed out waiting for replies from the test cluster", e);
        }

        if (replies.size() == 0) {
            throw new NotEnoughRepliesException("Not enough replies when trying to execute a command on test cluster");
        }

        for (MaestroNote reply : replies) {
            if (reply instanceof InternalError) {
                InternalError ie = (InternalError) reply;
                throw new MaestroException("Error executing a command on the test cluster: %s", ie.getMessage());
            }
        }
    }


    public static <T> void exec(Function<T, CompletableFuture<List<? extends MaestroNote>>> function, T value) {
        set(function, value);
    }

    public static <T, U> void exec(BiFunction<T, U, CompletableFuture<List<? extends MaestroNote>>> function, T value1, U value2) {
        set(function, value1, value2);
    }


    /**
     * Gets all the peers connected to the test cluster
     * @return A set of all known peers
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     * @throws InterruptedException if interrupted
     */
    public PeerSet getPeers() throws MaestroConnectionException, InterruptedException {
        logger.debug("Sending ping request");
        CompletableFuture<List<? extends MaestroNote>> repliesFuture = pingRequest(MaestroTopics.PEER_TOPIC);

        List<? extends MaestroNote> replies;
        try {
            replies = repliesFuture.get();
        } catch (ExecutionException e) {
            throw new MaestroException("Unable to collect peers", e);
        }

        Map<String, PeerInfo> knownPeers = new LinkedHashMap<>(replies.size());

        for (MaestroNote note : replies) {
            if (note instanceof PingResponse) {
                logger.debug("Accounting peer: {}/{}", ((PingResponse) note).getPeerInfo().prettyName(),
                        ((PingResponse) note).getId());
                knownPeers.put(((PingResponse) note).getId(), ((PingResponse) note).getPeerInfo());
            }
        }

        logger.info("Known peers recorded: {}", knownPeers.size());
        return new PeerSet(knownPeers);
    }
}
