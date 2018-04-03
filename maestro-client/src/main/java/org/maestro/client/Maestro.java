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

import org.maestro.client.exchange.MaestroMqttClient;
import org.maestro.common.client.MaestroRequester;
import org.maestro.common.client.notes.GetOption;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.client.MaestroClient;
import org.maestro.client.exchange.MaestroCollectorExecutor;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.notes.*;

import java.util.LinkedList;
import java.util.List;


/**
 * A maestro instance
 */
public final class Maestro implements MaestroRequester {
    private MaestroClient maestroClient;
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
        maestroClient.disconnect();
        collectorExecutor.stop();
        try {
            collectorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a flush request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void flushRequest() throws MaestroConnectionException {
        flushRequest(MaestroTopics.ALL_DAEMONS);
    }


    /**
     * Sends a flush request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void flushRequest(final String topic) throws MaestroConnectionException {
        FlushRequest maestroNote = new FlushRequest();

        maestroClient.publish(topic, maestroNote);
    }

    /**
     * Sends a ping request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void pingRequest() throws MaestroConnectionException {
        pingRequest(MaestroTopics.ALL_DAEMONS);
    }


    /**
     * Sends a ping request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void pingRequest(final String topic) throws MaestroConnectionException {
        PingRequest maestroNote = new PingRequest();

        maestroClient.publish(topic, maestroNote);
    }


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setBroker(final String value) throws MaestroConnectionException {
        setBroker(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setBroker(final String topic, final String value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setBroker(value);

        maestroClient.publish(topic, maestroNote);
    }


    /**
     * Sends a set duration request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroException if unable to send the MQTT request
     */
    public void setDuration(final Object value) throws MaestroException {
        setDuration(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set duration request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroException if unable to send the MQTT request
     */
    public void setDuration(final String topic, final Object value) throws MaestroException {
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
    }



    /**
     * Sends a set log level request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setLogLevel(final String value) throws MaestroConnectionException {
        setLogLevel(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set log level request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setLogLevel(final String topic, final String value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setLogLevel(value);

        maestroClient.publish(topic, maestroNote);
    }


    /**
     * Sends a set parallel count request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setParallelCount(final int value) throws MaestroConnectionException {
        setParallelCount(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set parallel count request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setParallelCount(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();


        maestroNote.setParallelCount(Integer.toString(value));

        maestroClient.publish(topic, maestroNote);
    }

    /**
     * Sends a set message size request (this one can be used for fixed or variable message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setMessageSize(final String value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setMessageSize(value);

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }


    /**
     * Sends a set message size request (this one can be used for fixed message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setMessageSize(final long value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setMessageSize(Long.toString(value));

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }


    /**
     * Sends a set throttle request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setThrottle(final int value) throws MaestroConnectionException {
        setThrottle(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set throttle request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setThrottle(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setThrottle(Integer.toString(value));

        maestroClient.publish(topic, maestroNote);
    }


    /**
     * Sends a set rate request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setRate(final int value) throws MaestroConnectionException {
        setRate(MaestroTopics.ALL_DAEMONS, value);
    }


    /**
     * Sends a set rate request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setRate(final String topic, final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setRate(Integer.toString(value));

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }


    /**
     * Sends a set fail-condition-latency (FCL) request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void setFCL(final int value) throws MaestroConnectionException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setFCL(Integer.toString(value));

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }


    /**
     * Sets the management interface URL
     * @param value The value to set the (remote) parameter to
     * @throws MaestroException if unable to send the MQTT request
     */
    public void setManagementInterface(final String value) throws MaestroException {
        SetRequest maestroNote = new SetRequest();

        maestroNote.setManagementInterface(value);

        maestroClient.publish(MaestroTopics.INSPECTOR_DAEMONS, maestroNote);
    }


    /**
     * Sends a start inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void startInspector() throws MaestroConnectionException {
        StartInspector maestroNote = new StartInspector();

        maestroClient.publish(MaestroTopics.INSPECTOR_DAEMONS, maestroNote);
    }


    /**
     * Sends a stop inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void stopInspector() throws MaestroConnectionException {
        StopInspector maestroNote = new StopInspector();

        maestroClient.publish(MaestroTopics.INSPECTOR_DAEMONS, maestroNote);
    }


    /**
     * Sends a start sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void startSender() throws MaestroConnectionException {
        StartSender maestroNote = new StartSender();

        maestroClient.publish(MaestroTopics.SENDER_DAEMONS, maestroNote);
    }


    /**
     * Sends a stop sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void stopSender() throws MaestroConnectionException {
        StopSender maestroNote = new StopSender();

        maestroClient.publish(MaestroTopics.SENDER_DAEMONS, maestroNote);
    }

    /**
     * Sends a start receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void startReceiver() throws MaestroConnectionException {
        StartReceiver maestroNote = new StartReceiver();

        maestroClient.publish(MaestroTopics.RECEIVER_DAEMONS, maestroNote);
    }


    /**
     * Sends a stop receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void stopReceiver() throws MaestroConnectionException {
        StopReceiver maestroNote = new StopReceiver();

        maestroClient.publish(MaestroTopics.RECEIVER_DAEMONS, maestroNote);
    }


    /**
     * Sends a stats request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void statsRequest() throws MaestroConnectionException {
        StatsRequest maestroNote = new StatsRequest();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }


    /**
     * Sends a halt request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void halt() throws MaestroConnectionException {
        Halt maestroNote = new Halt();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }


    /**
     * Sends a get request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void getDataServer() throws MaestroConnectionException {
        GetRequest maestroNote = new GetRequest();

        maestroNote.setGetOption(GetOption.MAESTRO_NOTE_OPT_GET_DS);

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }

    /**
     * Sends a start agent request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void startAgent() throws MaestroConnectionException {
        StartAgent maestroNote = new StartAgent();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }

    /**
     * Sends a stop agent request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void stopAgent() throws MaestroConnectionException {
        StopAgent maestroNote = new StopAgent();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }

    /**
     * Sends a user command request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void userCommand(final long option, final String payload) throws MaestroConnectionException {
        UserCommand1Request maestroNote = new UserCommand1Request();

        maestroNote.set(option, payload);


        maestroClient.publish(MaestroTopics.AGENT_DAEMONS, maestroNote);
    }

    /**
     * Sends a source request to the agent (which causes it to download the given source)
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    public void sourceRequest(final String sourceUrl, final String branch) throws MaestroConnectionException {
        AgentSourceRequest maestroNote = new AgentSourceRequest();

        maestroNote.setSourceUrl(sourceUrl);
        maestroNote.setBranch(branch);

        maestroClient.publish(MaestroTopics.AGENT_DAEMONS, maestroNote);
    }

    /**
     * Collect replies
     * @return A list of serialized maestro replies
     */
    public List<MaestroNote> collect() {
        return collectorExecutor.collect();
    }

    private boolean hasReplies(List<?> replies) {
        return (replies != null && replies.size() > 0);
    }


    /**
     * Collect replies up to a certain limit of retries/timeout
     * @param wait how much time between each retry
     * @param retries number of retries
     * @return A list of serialized maestro replies or null if none
     */
    public List<MaestroNote> collect(long wait, int retries) {
        List<MaestroNote> replies;

        do {
            replies = collectorExecutor.collect();

            if (hasReplies(replies)) {
                break;
            }

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retries--;
        } while (retries > 0);

        return replies;
    }

    /**
     * Collect replies up to a certain limit of retries/timeout
     * @param wait how much time between each retry
     * @param retries number of retries
     * @param expect The number of replies to expect.
     * @return A list of serialized maestro replies or null if none. May return less that expected.
     */
    public List<MaestroNote> collect(long wait, int retries, int expect) {
        List<MaestroNote> replies = new LinkedList<>();

        do {
            List<MaestroNote> collected = collectorExecutor.collect();

            if (collected != null) {
                replies.addAll(collected);

                if (hasReplies(replies) && replies.size() >= expect) {
                    break;
                }
            }

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retries--;
        } while (retries > 0);

        return replies;
    }
}
