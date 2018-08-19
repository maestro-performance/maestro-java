package org.maestro.client.exchange.support;

import org.maestro.common.NetworkUtils;
import org.maestro.common.Role;
import org.maestro.common.exceptions.MaestroException;

import java.net.UnknownHostException;

public class CollectorPeer implements PeerInfo {
    private String hostname;

    @SuppressWarnings("unused")
    @Override
    public void setRole(Role role) {
        // no-op
    }

    @Override
    public Role getRole() {
        return Role.OTHER;
    }

    @Override
    public String peerName() {
        return "maestro-java-collector";
    }

    @Override
    public String peerHost() {
        if (hostname == null) {
            try {
                hostname = NetworkUtils.getHost("maestro.worker.host");
            } catch (UnknownHostException e) {
                throw new MaestroException(e);
            }
        }

        return hostname;
    }

    @Override
    public GroupInfo groupInfo() {
        return new GroupInfo() {
            @Override
            public String memberName() {
                return "collector";
            }

            @Override
            public String groupName() {
                return "client";
            }
        };
    }
}
