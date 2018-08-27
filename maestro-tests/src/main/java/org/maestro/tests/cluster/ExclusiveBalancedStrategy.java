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
import org.maestro.common.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the same as the normal balanced distribution strategy, but it uses exclusive addressing for
 * each node. That way, parameters can be handled on a per-node basis.
 */
public class ExclusiveBalancedStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ExclusiveBalancedStrategy.class);
    private int counter = 0;

    public ExclusiveBalancedStrategy(final Maestro maestro) {
        super(maestro);
    }

    @Override
    protected PeerEndpoint assign(final String id, final PeerInfo peerInfo) {
        String topic = MaestroTopics.peerTopic(id);

        if (peerInfo.getRole() == Role.AGENT || peerInfo.getRole() == Role.INSPECTOR) {
            return new PeerEndpoint(peerInfo.getRole(), topic);
        }

        if (counter % 2 == 0) {
            logger.info("Assigning node {}@{} as {}", peerInfo.peerName(), peerInfo.peerHost(),  "receiver");
            Maestro.exec(getMaestro()::roleAssign, topic, Role.RECEIVER);

            counter++;
            return new PeerEndpoint(Role.RECEIVER, topic);
        }
        else {
            logger.info("Assigning node {}@{} as {}", peerInfo.peerName(), peerInfo.peerHost(),  "sender");
            Maestro.exec(getMaestro()::roleAssign, topic, Role.SENDER);

            counter++;
            return new PeerEndpoint(Role.SENDER, topic);
        }
    }

}
