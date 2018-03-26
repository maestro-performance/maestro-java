package org.maestro.client.notes;


/**
 * Visitor that handles sender-specific {@link MaestroEvent} instances.
 */
@SuppressWarnings("unused")
public interface MaestroSenderEventListener {

    void handle(StartSender note);

    void handle(StopSender note);
}
