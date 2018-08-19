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
 * The legacy strategy retain the fixed assignment strategy that was used
 * prior to Maestro 1.5. In this strategy, the workers have predefined roles
 * (determined by starting them with -Dmaestro.worker.role=sender).
 *
 * Likely more useful for development testing.
 */
public class LegacyStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(LegacyStrategy.class);

    public LegacyStrategy(final Maestro maestro) {
        super(maestro);
    }

    @Override
    protected PeerEndpoint assign(final String id, final PeerInfo peerInfo) {
        String topic = MaestroTopics.peerTopic(id);

        Role role = Role.hostTypeByName(peerInfo.peerName());

        if (role.isWorker()) {
            logger.info("Assigning node {}@{} as {}", peerInfo.peerName(), peerInfo.peerHost(),  peerInfo.peerName());
            Maestro.exec(getMaestro()::roleAssign, topic, role);
        }

        return new PeerEndpoint(role, MaestroTopics.peerTopic(role));
    }
}
