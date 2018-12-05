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

package org.maestro.tests.cluster;

import org.maestro.client.Maestro;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.exchange.support.PeerSet;
import org.maestro.common.client.exceptions.NotEnoughRepliesException;
import org.maestro.common.exceptions.MaestroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A simple distribution strategy that assign roles to the available nodes
 * on the test cluster.
 */
public abstract class AbstractStrategy implements DistributionStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AbstractStrategy.class);
    private final Maestro maestro;

    private PeerSet peers;
    private final Set<PeerEndpoint> endpoints = new LinkedHashSet<>();

    public AbstractStrategy(final Maestro maestro) {
        this.maestro = maestro;
    }

    /**
     * Assign a role to the node identified by the id and peer information
     * @param id peer ID
     * @param peerInfo peer information
     * @return A PeerEndpoint for addressing the distributed node
     */
    protected abstract PeerEndpoint assign(final String id, final PeerInfo peerInfo);

    private void unassign(final String id, final PeerInfo peerInfo) {
        if (peerInfo.getRole().isWorker()) {
            String topic = MaestroTopics.peerTopic(id);

            logger.info("Unassigning node {}@{} as {}", peerInfo.peerName(), peerInfo.peerHost(), peerInfo.getRole());
            try {
                Maestro.exec(maestro::roleUnassign, topic);
            }
            catch (NotEnoughRepliesException e) {
                logger.error("Not enough replies trying to unassign node {}@{} as {}", peerInfo.peerName(), peerInfo.peerHost(),
                        peerInfo.getRole());
            }
        }
    }

    @Override
    public synchronized PeerSet distribute(final PeerSet peers) {
        if (peers.available() == 0) {
            throw new MaestroException("There are not enough available peers to distribute");
        }

        // Peers not yet distributed
        if (endpoints.size() == 0) {
            peers.getPeers().forEach((k, v) -> endpoints.add(assign(k, v)));
        }

        try {
            this.peers = maestro.getPeers();
        } catch (InterruptedException e) {
            throw new MaestroException("Interrupted while updating the peer set");
        }

        if (this.peers.workers() == 0) {
            throw new MaestroException("There are not enough peers to run the test");
        }

        return this.peers;
    }


    @Override
    public void reset() {
        // Reset peer assignment
        if (endpoints.size() > 0) {
            peers.getPeers().forEach(this::unassign);
            this.peers = null;
            this.endpoints.clear();
        }
    }

    @Override
    public synchronized Set<PeerEndpoint> endpoints() {
        return Collections.unmodifiableSet(endpoints);
    }

    protected Maestro getMaestro() {
        return maestro;
    }
}
