package org.maestro.worker.tests.support.runner;

import org.maestro.client.exchange.support.GroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.Role;

public class SenderInfo implements PeerInfo {
    @Override
    public void setRole(Role role) {
        //
    }

    @Override
    public Role getRole() {
        return Role.SENDER;
    }

    @Override
    public String peerName() {
        return "sender";
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
