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


public class NonAssigningStrategy extends AbstractStrategy {

    public NonAssigningStrategy(Maestro maestro) {
        super(maestro);
    }

    @Override
    protected PeerEndpoint assign(String id, PeerInfo peerInfo) {
        String topic = MaestroTopics.peerTopic(id);

        return new PeerEndpoint(peerInfo.getRole(), topic);
    }
}
