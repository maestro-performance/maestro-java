package org.maestro.client.exchange.support;

import org.maestro.common.NetworkUtils;
import org.maestro.common.exceptions.MaestroException;

import java.net.UnknownHostException;

public class CollectorPeer implements PeerInfo {
    private String hostname;

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
}
