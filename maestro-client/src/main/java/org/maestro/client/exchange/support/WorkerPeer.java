package org.maestro.client.exchange.support;

public class WorkerPeer implements PeerInfo {
    private String name;
    private String host;

    public WorkerPeer(final String name, final String host) {
        this.name = name;
        this.host = host;
    }

    @Override
    public String peerName() {
        return name;
    }

    @Override
    public String peerHost() {
        return host;
    }
}
