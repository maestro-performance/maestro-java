

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

package org.maestro.reports.organizer;

import org.junit.Test;
import org.maestro.client.exchange.support.GroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.Role;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class DefaultOrganizerTest {
    private class TestPeerInfo implements PeerInfo {
        private final Role role;


        public TestPeerInfo(Role role) {
            this.role = role;
        }

        @Override
        public void setRole(Role role) {

        }

        @Override
        public Role getRole() {
            return role;
        }

        @Override
        public String peerName() {
            return null;
        }

        @Override
        public String peerHost() {
            return "localhost";
        }

        @Override
        public GroupInfo groupInfo() {
            return null;
        }
    }


    @Test
    public void testOrganizer() {

        DefaultOrganizer defaultOrganizer = new DefaultOrganizer("/sample");

        assertTrue("Default tracker must not be null",
                defaultOrganizer.getTracker() != null);

        defaultOrganizer.setResultType("failed");

        PeerInfo receiverPeerInfo = new TestPeerInfo(Role.RECEIVER);
        String sampleReceiver = defaultOrganizer.organize(receiverPeerInfo);
        assertEquals("Unexpected directory layout", "/sample/receiver/failed/0/localhost",
                sampleReceiver);

        defaultOrganizer.setResultType("success");

        PeerInfo senderPeerInfo = new TestPeerInfo(Role.SENDER);
        String sampleSender = defaultOrganizer.organize(senderPeerInfo);
        assertEquals("Unexpected directory layout", "/sample/sender/success/0/localhost",
                sampleSender);
    }
}
