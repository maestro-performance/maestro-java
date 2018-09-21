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
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The balanced distribution strategy collects all the available nodes and distributed
 * them evenly throughout the test cluster. That means: if there are 2 workers, there
 * will be 1 receiver and 1 sender, if there are 4, then 2 receiver and 2 senders and
 * so on.
 */
public class BalancedStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(BalancedStrategy.class);
    private int counter = 0;

    public BalancedStrategy(final Maestro maestro) {
        super(maestro);
    }

    @Override
    protected PeerEndpoint assign(final String id, final PeerInfo peerInfo) {
        String topic = MaestroTopics.peerTopic(id);

        if (peerInfo.getRole() == Role.AGENT || peerInfo.getRole() == Role.INSPECTOR || peerInfo.getRole() == Role.REPORTS_SERVER) {
            return new PeerEndpoint(peerInfo.getRole(), topic);
        }

        if (counter % 2 == 0) {
            logger.info("Assigning node {}@{} as {}", peerInfo.peerName(), peerInfo.peerHost(),  "receiver");
            Maestro.exec(getMaestro()::roleAssign, topic, Role.RECEIVER);

            counter++;
            return new PeerEndpoint(Role.RECEIVER, MaestroTopics.peerTopic(Role.RECEIVER));
        }
        else {
            logger.info("Assigning node {}@{} as {}", peerInfo.peerName(), peerInfo.peerHost(),  "sender");
            Maestro.exec(getMaestro()::roleAssign, topic, Role.SENDER);

            counter++;
            return new PeerEndpoint(Role.SENDER, MaestroTopics.peerTopic(Role.SENDER));
        }
    }

}
