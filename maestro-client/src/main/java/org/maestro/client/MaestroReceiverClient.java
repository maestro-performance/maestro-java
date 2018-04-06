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

import org.maestro.client.exchange.MaestroMqttClient;
import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.duration.EpochClocks;
import org.maestro.common.duration.EpochMicroClock;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MaestroReceiverClient extends MaestroMqttClient implements MaestroReceiver {
    private static final Logger logger = LoggerFactory.getLogger(MaestroReceiverClient.class);

    private final EpochMicroClock epochMicroClock;
    private final String clientName;
    private final String host;
    private final String id;

    public MaestroReceiverClient(String url, final String clientName, final String host, final String id) throws MaestroException {
        super(url);

        this.clientName = clientName;
        this.host = host;
        this.id = id;
        //it is supposed to be used just by one thread
        this.epochMicroClock = EpochClocks.exclusiveMicro();
    }

    public void replyOk() {
        logger.trace("Sending the OK response from {}", this.toString());
        OkResponse okResponse = new OkResponse();

        okResponse.setName(clientName + "@" + host);
        okResponse.setId(id);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, okResponse);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response {}", e.getMessage(), e);
        }
    }

    public void replyInternalError() {
        logger.trace("Sending the internal error response from {}", this.toString());
        InternalError errResponse = new InternalError();

        errResponse.setName(clientName + "@" + host);
        errResponse.setId(id);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, errResponse);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response: {}", e.getMessage(), e);
        }
    }

    public void pingResponse(long sec, long uSec) {
        logger.trace("Creation seconds.micro: {}.{}", sec, uSec);

        final long creationEpochMicros = TimeUnit.SECONDS.toMicros(sec) + uSec;
        final long nowMicros = epochMicroClock.microTime();
        final long elapsedMicros = nowMicros - creationEpochMicros;

        logger.trace("Elapsed: {}", elapsedMicros);
        PingResponse response = new PingResponse();

        response.setElapsed(TimeUnit.MICROSECONDS.toMillis(elapsedMicros));
        response.setName(clientName + "@" + host);
        response.setId(id);

        super.publish(MaestroTopics.MAESTRO_TOPIC, response);
    }


    @Override
    public void notifySuccess(String message) {
        logger.trace("Sending the test success notification from {}", this.toString());
        TestSuccessfulNotification notification = new TestSuccessfulNotification();

        notification.setName(clientName + "@" + host);
        notification.setId(id);

        notification.setMessage(message);

        try {
            super.publish(MaestroTopics.NOTIFICATION_TOPIC, notification, 0, true);
        } catch (Exception e) {
            logger.error("Unable to publish the success notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void notifyFailure(String message) {
        logger.trace("Sending the test success notification from {}", this.toString());
        TestFailedNotification notification = new TestFailedNotification();

        notification.setName(clientName + "@" + host);
        notification.setId(id);

        notification.setMessage(message);

        try {
            super.publish(MaestroTopics.NOTIFICATION_TOPIC, notification, 0, true);
        } catch (Exception e) {
            logger.error("Unable to publish the failure notification: {}", e.getMessage(), e);
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
        statsResponse.setName(clientName + "@" + host);
        statsResponse.setId(id);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, statsResponse, 0, false);
        } catch (Exception e) {
            logger.error("Unable to publish the status response: {}", e.getMessage(), e);
        }
    }


    /**
     * Publishes a get response as a reply to a get request
     * @param getResponse the get response to publish
     */
    public void getResponse(final GetResponse getResponse) {
        getResponse.setName(clientName + "@" + host);
        getResponse.setId(id);

        try {
            super.publish(MaestroTopics.MAESTRO_TOPIC, getResponse, 0, false);
        } catch (Exception e) {
            logger.error("Unable to publish the get response: {}", e.getMessage(), e);
        }
    }

    /**
     *  Publishes a agent general response as a reply to a agent general request
     * @param userCommand1Response the agent general response to publish
     */
    public void AgentGeneralResponse(final UserCommand1Response userCommand1Response) {
        userCommand1Response.setName(clientName + "@" + host);
        userCommand1Response.setId(id);

        // @TODO jstejska: delete after test
        logger.debug(userCommand1Response.toString());

        super.publish(MaestroTopics.MAESTRO_TOPIC, userCommand1Response);
    }
}
