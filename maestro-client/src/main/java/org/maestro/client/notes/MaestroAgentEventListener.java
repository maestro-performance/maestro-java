package org.maestro.client.notes;


/**
 * Visitor that handles agent-specific {@link MaestroEvent} instances.
 */
@SuppressWarnings("unused")
public interface MaestroAgentEventListener {
    void handle(StartAgent note);

    void handle(StopAgent note);

    void handle(AgentGenericRequest note);

    void handle(AgentSourceRequest note);
}
