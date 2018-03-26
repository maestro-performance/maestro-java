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

package org.maestro.client.exchange;

/**
 * Maestro topics contains the list of all topics used by Maestro
 */
public class MaestroTopics {
    /**
     * This topic is used to publish the peer responses for a maestro request
     */
    public final static String MAESTRO_TOPIC = "/mpt/maestro";

    /**
     * This topic is used by the peers to send notifications
     */
    public final static String NOTIFICATION_TOPIC = "/mpt/notifications";

    /**
     * This topic is used to publish requests for all daemons
     */
    public final static String ALL_DAEMONS = "/mpt/daemon";

    /**
     * This topic is used to publish requests for sender daemons
     */
    public final static String SENDER_DAEMONS = "/mpt/daemon/sender";

    /**
     * This topic is used to publish requests for receiver daemons
     */
    public final static String RECEIVER_DAEMONS ="/mpt/daemon/receiver";

    /**
     * This topic is used to publish requests for inspector daemons
     */
    public final static String INSPECTOR_DAEMONS = "/mpt/daemon/inspector";

    /**
     * This topic is used to publish requests for agent daemons
     */
    public final static String AGENT_DAEMONS = "/mpt/daemon/agent";

    /**
     * These topics are the ones subscribed by a Maestro client
     */
    public final static String[] MAESTRO_TOPICS = {MAESTRO_TOPIC, NOTIFICATION_TOPIC};

    /**
     * These topics are the ones subscribed by a Maestro sender
     */
    public final static String[] MAESTRO_SENDER_TOPICS = {ALL_DAEMONS, NOTIFICATION_TOPIC, SENDER_DAEMONS};

    /**
     * These topics are the ones subscribed by a Maestro receiver
     */
    public final static String[] MAESTRO_RECEIVER_TOPICS = {ALL_DAEMONS, NOTIFICATION_TOPIC, RECEIVER_DAEMONS};

    /**
     * These topics are the ones subscribed by a Maestro inspector
     */
    public final static String[] MAESTRO_INSPECTOR_TOPICS = {ALL_DAEMONS, NOTIFICATION_TOPIC, INSPECTOR_DAEMONS};

    /**
     * These topics are the ones subscribed by a Maestro agent
     */
    public final static String[] MAESTRO_AGENT_TOPICS = {ALL_DAEMONS, NOTIFICATION_TOPIC, AGENT_DAEMONS};

    private MaestroTopics() {}
}
