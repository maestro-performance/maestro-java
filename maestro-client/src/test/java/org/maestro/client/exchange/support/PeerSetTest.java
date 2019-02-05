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

import org.junit.Before;
import org.junit.Test;
import org.maestro.common.Role;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PeerSetTest {
    private Map<String, PeerInfo> knownPeers = new LinkedHashMap<>(10);
    private PeerSet peerSet;

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 10; i++) {
            Role role = (i % 2 == 0) ? Role.SENDER : Role.RECEIVER;

            knownPeers.put(String.valueOf(i), new WorkerPeer(String.format("worker%d",i), String.format("host%d", i), role));
        }

        knownPeers.put("10", new WorkerPeer("worker10", "host10", Role.OTHER));
        knownPeers.put("11", new WorkerPeer("worker11", "host11", Role.OTHER));
        knownPeers.put("12", new WorkerPeer("agent", "host12", Role.INSPECTOR));
        knownPeers.put("13", new WorkerPeer("inspector", "host13", Role.AGENT));

        peerSet = new PeerSet(knownPeers);
    }

    @Test
    public void getPeers() {
        assertNotNull("The list of peers must note be null", peerSet.getPeers());
        assertEquals("The number of peers does not match ", 14, peerSet.getPeers().size());
    }

    @Test
    public void available() {
        assertEquals("The number of available nodes does not match", 2, peerSet.available());
    }

    @Test
    public void workers() {
        assertEquals("The number of worker nodes does not match", 10, peerSet.workers());

    }

    @Test
    public void count() {
        assertEquals("The number of agent nodes does not match", 1, peerSet.count(Role.AGENT));
        assertEquals("The number of inspector nodes does not match", 1, peerSet.count(Role.INSPECTOR));
        assertEquals("The number of receiver nodes does not match", 5, peerSet.count(Role.RECEIVER));

        assertEquals("The number of agent + inspector nodes does not match", 2, peerSet.count(Role.AGENT, Role.INSPECTOR));
    }
}