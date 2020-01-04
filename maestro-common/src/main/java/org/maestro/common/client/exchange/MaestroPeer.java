package org.maestro.common.client.exchange;

public interface MaestroPeer {
    void connect();
    void subscribe(final String[] topics);
    boolean isRunning();
    void disconnect();

}
