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

package org.maestro.client.exchange.support;

import org.maestro.common.Role;

import java.util.Objects;

/**
 * Represents the Maestro broker endpoint to communicate with a peer or a group of peers
 */
public class PeerEndpoint {
    private final Role role;
    private final String destination;

    public PeerEndpoint(Role role, String destination) {
        this.role = role;
        this.destination = destination;
    }

    public Role getRole() {
        return role;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerEndpoint that = (PeerEndpoint) o;
        return role == that.role &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, destination);
    }
}
