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

import org.maestro.client.exchange.support.GroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.Role;

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
     * This topic is used to publish the peer responses for a maestro request
     */
    public final static String PEER_TOPIC = "/mpt/peer";

    /**
     * This topic addresses all worker peers
     */
    public final static String WORKERS_TOPIC = PEER_TOPIC + "/worker";

    /**
     * These topics are the ones subscribed by a Maestro client
     */
    public final static String[] MAESTRO_TOPICS = {MAESTRO_TOPIC, NOTIFICATION_TOPIC};


    private MaestroTopics() {}

    /**
     * Returns the full list of topics for a peer including the public and private topics (those that are specific
     * to that peer)
     * @param publicTopics The topics that are public to all peers
     * @param peerInfo the peer information container
     * @param id the peer ID
     * @return an array with the topics for the given peer
     */
    public static String[] peerTopics(final String[] publicTopics, final PeerInfo peerInfo,
                                      final String id) {
        String[] ret = new String[publicTopics.length + 2];

        ret[0] = PEER_TOPIC + "/by-name/" + peerInfo.peerHost() + "/" + peerInfo.peerName();
        ret[1] = PEER_TOPIC + "/by-id/" + id;

        System.arraycopy(publicTopics, 0, ret, 2, publicTopics.length);

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
    public static String[] peerTopics(final String id) {
        return new String[] { peerTopic(id) ,
                WORKERS_TOPIC + "/",
                PEER_TOPIC
        };
    }

    /**
     * Get the peer specific topic by name and host
     * @param groupInfo group information
     * @return The peer-specific topic (for that client name)
     */
    public static String peerTopic(final GroupInfo groupInfo) {
        return PEER_TOPIC + "/by-group/" + groupInfo.groupName() + "/" + groupInfo.memberName();
    }

    /**
     * Get the peer specific topic by id
     * @param id peer id
     * @return The peer-specific topic (for that client id)
     */
    public static String peerTopic(final String id) {
        return PEER_TOPIC + "/by-id/" + id;
    }

    /**
     * Get the peer specific topic by id
     * @param peerInfo the peer information container
     * @return The peer-specific topic (for that client id)
     */
    public static String peerTopic(final PeerInfo peerInfo) {
        return PEER_TOPIC + "/by-name/" + peerInfo.peerHost() + "/" + peerInfo.peerName() + "/";
    }


    /**
     * Get the role-specific topic
     * @param role the peer role
     * @return The peer-specific topic (for that client id)
     */
    public static String peerTopic(final Role role) {
        String roleName = role.toString();

        return PEER_TOPIC + "/by-role/" + roleName + "/";
    }

    public static String[] inspectorTopics(final String id, final PeerInfo peerInfo) {
        return new String[] {
                PEER_TOPIC,
                NOTIFICATION_TOPIC,
                MaestroTopics.peerTopic(Role.INSPECTOR),
                peerTopic(id),
                peerTopic(peerInfo),

        };
    }


    public static String[] agentTopics(final String id, final PeerInfo peerInfo) {
        return new String[] {
                PEER_TOPIC,
                NOTIFICATION_TOPIC,
                MaestroTopics.peerTopic(Role.AGENT),
                peerTopic(id),
                peerTopic(peerInfo),
        };
    }

}
