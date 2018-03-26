package org.maestro.client.notes;


/**
 * Visitor that handles receiver-specific {@link MaestroEvent} instances.
 */
@SuppressWarnings("unused")
public interface MaestroReceiverEventListener {

    void handle(StartReceiver note);

    void handle(StopReceiver note);
}
