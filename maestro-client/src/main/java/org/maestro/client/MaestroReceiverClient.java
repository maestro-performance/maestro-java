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

package org.maestro.client;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.exchange.MaestroMqttClient;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.MqttServiceLevel;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.client.notes.ErrorCode;
import org.maestro.common.client.notes.LocationTypeInfo;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.Test;
import org.maestro.common.duration.EpochClocks;
import org.maestro.common.duration.EpochMicroClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * A maestro client that receives data
 */
public class MaestroReceiverClient extends MaestroMqttClient implements MaestroReceiver {
    /**
     * A callback object for throttling the sending of the log data (to prevent flooding
     * or abusing the Maestro Broker resources)
     */
    private static class ThrottleCallback implements MaestroNoteCallback {
        private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
        private static final Logger logger = LoggerFactory.getLogger(ThrottleCallback.class);

        private static final int defaultDelay;

        static {
            defaultDelay = config.getInteger("worker.throttle.delay", 100);
        }

        @Override
        public boolean call(MaestroNote note) {
            try {
                Thread.sleep(defaultDelay);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for the note send delay");
            }

            return true;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MaestroReceiverClient.class);

    private final EpochMicroClock epochMicroClock;
    private final PeerInfo peerInfo;
    private final String id;

    /**
     * Constructor
     * @param url the broker URL
     * @param peerInfo the peer information
     * @param id the client ID
     */
    public MaestroReceiverClient(final String url, final PeerInfo peerInfo, final String id) {
        super(url);

        this.peerInfo = peerInfo;
        this.id = id;
        //it is supposed to be used just by one thread
        this.epochMicroClock = EpochClocks.exclusiveMicro();
    }


    /**
     * Gets the client id
     * @return the client id
     */
    public String getId() {
        return id;
    }


    @Override
    public void replyOk(final MaestroNote note) {
        logger.trace("Sending the OK response from {}", this.toString());
        OkResponse okResponse = new OkResponse();

        okResponse.setPeerInfo(peerInfo);
        okResponse.setId(id);
        okResponse.correlate(note);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, okResponse);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response {}", e.getMessage(), e);
        }
    }


    @Override
    public void replyInternalError(final MaestroNote note, final String message, final Object...args) {
        replyInternalError(note, ErrorCode.UNSPECIFIED, message, args);
    }


    @Override
    public void replyInternalError(final MaestroNote note, final ErrorCode errorCode, final String message,
                                   final Object...args)
    {
        logger.trace("Sending the internal error response from {}", this.toString());
        InternalError errResponse = new InternalError(errorCode, String.format(message, (Object[]) args));

        errResponse.setPeerInfo(peerInfo);
        errResponse.setId(id);
        errResponse.correlate(note);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, errResponse, MqttServiceLevel.AT_LEAST_ONCE, false);
        } catch (Exception e) {
            logger.error("Unable to publish the internal error response: {}", e.getMessage(), e);
        }
    }

    @Override
    public void pingResponse(final MaestroNote note, long sec, long uSec) {
        logger.trace("Creation seconds.micro: {}.{}", sec, uSec);

        final long creationEpochMicros = TimeUnit.SECONDS.toMicros(sec) + uSec;
        final long nowMicros = epochMicroClock.microTime();
        final long elapsedMicros = nowMicros - creationEpochMicros;

        logger.trace("Elapsed: {}", elapsedMicros);
        PingResponse response = new PingResponse();

        response.setElapsed(TimeUnit.MICROSECONDS.toMillis(elapsedMicros));
        response.setPeerInfo(peerInfo);
        response.setId(id);
        response.correlate(note);

        super.publish(MaestroTopics.MAESTRO_TOPIC, response, MqttServiceLevel.AT_MOST_ONCE, false);
    }


    @Override
    public void notifySuccess(final Test test, final String message) {
        logger.info("Sending the test success notification for {}", test.getTestName());
        TestSuccessfulNotification notification = new TestSuccessfulNotification();

        notification.setPeerInfo(peerInfo);
        notification.setId(id);

        notification.setTest(test);
        notification.setMessage(message);

        try {
            super.publish(MaestroTopics.NOTIFICATION_TOPIC, notification, MqttServiceLevel.EXACTLY_ONCE, false);
        } catch (Exception e) {
            logger.error("Unable to publish the success notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyFailure(final Test test, final String message) {
        logger.info("Sending the test success notification for {}", test.getTestName());
        TestFailedNotification notification = new TestFailedNotification();

        notification.setPeerInfo(peerInfo);
        notification.setId(id);
        notification.setTest(test);

        notification.setMessage(message);

        try {
            super.publish(MaestroTopics.NOTIFICATION_TOPIC, notification, MqttServiceLevel.EXACTLY_ONCE, false);
        } catch (Exception e) {
            logger.error("Unable to publish the failure notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyStarted(final Test test, final String message) {
        logger.trace("Sending the test started notification for {}", test.getTestName());
        TestStartedNotification notification = new TestStartedNotification();

        notification.setPeerInfo(peerInfo);
        notification.setId(id);

        notification.setTest(test);
        notification.setMessage(message);

        try {
            super.publish(MaestroTopics.NOTIFICATION_TOPIC, notification, MqttServiceLevel.EXACTLY_ONCE, false);
        } catch (Exception e) {
            logger.error("Unable to publish the test started notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void abnormalDisconnect() {
        // TODO: handle abnormal disconnect and the LWT message
    }

    /**
     * Publishes a stats response as a reply to a stats request
     * @param statsResponse the stats response to publish
     */
    public void statsResponse(final StatsResponse statsResponse) {
        statsResponse.setPeerInfo(peerInfo);
        statsResponse.setId(id);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, statsResponse, MqttServiceLevel.AT_MOST_ONCE, false);
        } catch (Exception e) {
            logger.error("Unable to publish the status response: {}", e.getMessage(), e);
        }
    }


    /**
     * Publishes a get response as a reply to a get request
     * @param getResponse the get response to publish
     */
    public void getResponse(final GetResponse getResponse) {
        getResponse.setPeerInfo(peerInfo);
        getResponse.setId(id);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, getResponse, MqttServiceLevel.AT_MOST_ONCE, false);
        } catch (Exception e) {
            logger.error("Unable to publish the get response: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends log files via Maestro broker
     * @param logFile the log file to send
     * @param note the requesting note
     * @param hash the hash for the file being sent
     * @param locationTypeInfo information about the location of the log files
     * @param peerInfo information about the peer sending the files
     * @param id peer ID
     * @param client the MaestroReceiverClient instance to use to send the log data
     */
    public static void logResponse(final File logFile, final LogRequest note, final String hash,
                                   final LocationTypeInfo locationTypeInfo, final PeerInfo peerInfo,
                                   final String id, final MaestroReceiverClient client) {
        LogResponse logResponse = new LogResponse();

        logResponse.setPeerInfo(peerInfo);
        logResponse.setId(id);

        logResponse.setLocationType(note.getLocationType());
        logResponse.setLocationTypeInfo(locationTypeInfo);
        logResponse.setFile(logFile);
        logResponse.setFileHash(hash);
        logResponse.correlate(note);

        ThrottleCallback throttleCallback = new ThrottleCallback();

        client.publish(MaestroTopics.MAESTRO_LOGS_TOPIC, logResponse, MqttServiceLevel.EXACTLY_ONCE, false,
                throttleCallback);
    }


    /**
     * Notifies that the drain operation is finished
     * @param status whether the drain was successful (true) or not (false)
     * @param message Any message related to the drain completion status
     */
    public void notifyDrainComplete(boolean status, final String message) {
        logger.trace("Sending the drain complete notification from {}", this.toString());
        DrainCompleteNotification notification = new DrainCompleteNotification();

        notification.setPeerInfo(peerInfo);
        notification.setId(id);

        notification.setSuccessful(status);
        notification.setMessage(message);

        try {
            super.publish(MaestroTopics.NOTIFICATION_TOPIC, notification, MqttServiceLevel.AT_LEAST_ONCE, false);
        } catch (Exception e) {
            logger.error("Unable to publish the drain complete notification: {}", e.getMessage(), e);
        }
    }
}
