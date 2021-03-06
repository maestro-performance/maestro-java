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
 *
 */

package org.maestro.client.exchange.support;

import org.maestro.common.Role;

import java.util.Collections;
import java.util.Map;

/**
 * Maintains a set of peers
 */
public class PeerSet {
    private final Map<String, PeerInfo> peers;

    /**
     * Constructor
     * @param peers the peers
     */
    public PeerSet(Map<String, PeerInfo> peers) {
        this.peers = peers;
    }


    /**
     * Get the peers in this set
     * @return a map of peers key'd by their ID
     */
    public Map<String, PeerInfo> getPeers() {
        return Collections.unmodifiableMap(peers);
    }


    /**
     * Count the number of available (w/ unassigned roles/names) peers
     * @return the number of available peers
     */
    public long available() {
        return peers.values().stream().filter(peerInfo -> peerInfo.getRole() == Role.OTHER).count();
    }

    private boolean is(final PeerInfo peerInfo, final Role...roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }

        for (Role role : roles) {
            if (role.equals(peerInfo.getRole())) {
                return true;
            }
        }

        return false;
    }


    /**
     * Count the number of workers peers
     * @return the number of available peers
     */
    public long workers() {
        return count(Role.RECEIVER, Role.SENDER);
    }


    /**
     * Count the number of peers of a certain role
     * @param roles One or more roles to count for
     * @return the number of available peers
     */
    public long count(final Role...roles) {
        if (roles == null || roles.length == 0) {
            return 0;
        }

        return peers.values().stream().filter(p -> is(p, roles)).count();
    }
}
