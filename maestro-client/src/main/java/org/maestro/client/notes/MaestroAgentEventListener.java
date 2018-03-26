package org.maestro.client.notes;


/**
 * Visitor that handles agent-specific {@link MaestroEvent} instances.
 */
@SuppressWarnings("unused")
public interface MaestroAgentEventListener {
    void handle(StartAgent note);

    void handle(StopAgent note);

    void handle(AgentGeneralRequest note);

    void handle(AgentSourceRequest note);
}
