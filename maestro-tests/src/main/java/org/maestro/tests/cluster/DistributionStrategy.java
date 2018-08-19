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

package org.maestro.tests.cluster;

import org.maestro.client.Maestro;
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.client.exchange.support.PeerSet;

import java.util.Set;

/**
 * A distribution strategy allow test code to manage the distribute or
 * peer roles in a test cluster
 */
public interface DistributionStrategy {

    /**
     * Given a set of peers distribute them
     * @param peers the set of peers to distribute
     * @see Maestro#getPeers()
     * @return Implementations must return the updated set of peers
     */
    PeerSet distribute(final PeerSet peers);

    /**
     * Reset the distribution of peers
     */
    void reset();


    /**
     * Get the endpoint for lastly distributed peer set. These endpoint are used to
     * send requests to the workers (ie.: one endpoint per role, group, etc)
     * @return A set of endpoints
     */
    Set<PeerEndpoint> endpoints();
}
