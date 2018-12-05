/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.client.exchange.support;

import org.maestro.common.NetworkUtils;
import org.maestro.common.Role;
import org.maestro.common.exceptions.MaestroException;

import java.net.UnknownHostException;

public class CollectorPeer implements PeerInfo {
    private String hostname;

    @SuppressWarnings("unused")
    @Override
    public void setRole(Role role) {
        // no-op
    }

    @Override
    public Role getRole() {
        return Role.OTHER;
    }

    @Override
    public String peerName() {
        return "maestro-java-collector";
    }

    @Override
    public String peerHost() {
        if (hostname == null) {
            try {
                hostname = NetworkUtils.getHost("maestro.worker.host");
            } catch (UnknownHostException e) {
                throw new MaestroException(e);
            }
        }

        return hostname;
    }

    @Override
    public GroupInfo groupInfo() {
        return new GroupInfo() {
            @Override
            public String memberName() {
                return "collector";
            }

            @Override
            public String groupName() {
                return "client";
            }
        };
    }
}
