package org.maestro.client.notes;


/**
 * Visitor that handles inspector-specific {@link MaestroEvent} instances.
 */
@SuppressWarnings("unused")
public interface MaestroInspectorEventListener {
    void handle(StartInspector note);

    void handle(StopInspector note);
}
