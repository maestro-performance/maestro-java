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

package org.maestro.tests.support;

import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.common.Role;

import java.util.HashMap;
import java.util.Map;

/**
 * A resolver that provides test endpoints based on the input role
 */
public class RoleBasedEndpointResolver implements TestEndpointResolver {
    private final Map<Role, TestEndpoint> endpoints = new HashMap<>();

    @Override
    public void register(Role role, TestEndpoint testEndpoint) {
        endpoints.put(role, testEndpoint);
    }

    @Override
    public TestEndpoint resolve(PeerEndpoint peerEndpoint) {
        return endpoints.get(peerEndpoint.getRole());
    }
}
