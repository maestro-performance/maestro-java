package org.maestro.client.exchange;

import java.util.function.Consumer;

import org.maestro.common.client.notes.MaestroNote;

public interface ConsumerEndpoint<T extends MaestroNote> {
    String getClientId();

    boolean isConnected();

    void connect();
    void disconnect();
    void subscribe(String[] endpoints);

    void setConsumer(Consumer<T> noteArrivedConsumer);
}
