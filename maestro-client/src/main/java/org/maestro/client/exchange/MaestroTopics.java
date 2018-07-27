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
     * The topic used for log publication
     */
    public final static String MAESTRO_LOGS_TOPIC = "/mpt/logs";

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
     * This topic is used to publish the peer responses for a maestro request
     */
    public final static String PEER_TOPIC = "/mpt/peer";

    /**
     * These topics are the ones subscribed by a Maestro client
     */
    public final static String[] MAESTRO_TOPICS = {MAESTRO_TOPIC, NOTIFICATION_TOPIC};

    /**
     * Generic topics for workers
     */
    public final static String[] MAESTRO_WORKER_TOPICS = {ALL_DAEMONS, NOTIFICATION_TOPIC};

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

    /**
     * Returns the full list of topics for a peer including the public and private topics (those that are specific
     * to that peer)
     * @param publicTopics The topics that are public to all peers
     * @param clientName the client name
     * @param host the hostname for the peer
     * @param id the peer ID
     * @return an array with the topics for the given peer
     */
    public static String[] peerTopics(final String[] publicTopics, final String clientName, final String host,
                                      final String id) {
        String[] ret = new String[publicTopics.length + 2];

        ret[0] = PEER_TOPIC + "/by-name/" + host + "/" + clientName;
        ret[1] = PEER_TOPIC + "/by-id" + id;

        for (int i = 0; i < publicTopics.length; i++) {
            ret[i+2] = publicTopics[i];
        }

        return ret;
    }


    /**
     * Get the peer specific topic by name and host
     * @param clientName the client name
     * @param host the hostname for the peer
     * @return The peer-specific topic (for that client name)
     */
    public static String peerTopic(final String clientName, final String host) {
        return PEER_TOPIC + "/by-name/" + host + "/" + clientName;
    }


    /**
     * Get the peer specific topic by id
     * @param id the peer id
     * @return The peer-specific topic (for that client name)
     */
    public static String peerTopic(final String id) {
        return PEER_TOPIC + "/by-id/" + id;
    }
}
