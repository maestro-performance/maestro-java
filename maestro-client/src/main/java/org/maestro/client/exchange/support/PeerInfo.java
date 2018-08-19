package org.maestro.client.exchange.support;

import org.maestro.common.Role;

public interface PeerInfo {

    void setRole(Role role);

    Role getRole();

    String peerName();

    String peerHost();

    GroupInfo groupInfo();

    default String prettyName() {
        return peerName() + "@" + peerHost();
    }
}
