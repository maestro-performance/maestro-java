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

/**
 * This interface provides a why for resolving the test endpoints on a
 * per role basis.
 */
public interface TestEndpointResolver {

    /**
     * Get the test endpoint for the given peer endpoint
     * @param peerEndpoint the peer endpoint for which the test end point needs to be resolved
     * @return the test endpoint or null if not found
     */
    TestEndpoint resolve(PeerEndpoint peerEndpoint);

    /**
     * Register a test endpoint for the given role
     * @param role the role to be assigned to the given test endpoint
     * @param testEndpoint the test endpoint to assign to the given role
     */
    void register(Role role, TestEndpoint testEndpoint);
}
