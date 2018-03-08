package org.maestro.common.agent;

import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;

/**
 * Base interface for implementing Agent endpoints.
 */
public interface AgentEndpoint {

    /**
     * The main method executed by the endpoint.
     * @return null
     */
    Object handle();

    /**
     * Sets the content of the note associated with the external endpoint.
     * @param note the note
     */
    void setMaestroNote(MaestroNote note);

    /**
     * Sets the content of the client associated with the external endpoint.
     * @param client the maestro client
     */
    void setMaestroClient(MaestroClient client);
}
