package org.maestro.agent.base;

import org.maestro.common.agent.AgentEndpoint;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;

/**
 * Abstract handler class for external points scripts
 */
public abstract class AbstractHandler implements AgentEndpoint{

    private MaestroClient client;
    private MaestroNote note;

    /**
     * Sets the content of the note associated with the external endpoint
     * @param note the note
     */
    public void setMaestroNote(final MaestroNote note) {
        this.note = note;
    }

    /**
     * Get maestro note
     * @return MaestroNote
     */
    public MaestroNote getNote() {
        return note;
    }

    /**
     * Sets the content of the client associated with the external endpoint.
     * @param client the maestro client
     */
    public void setMaestroClient(final MaestroClient client) {
        this.client = client;
    }

    /**
     * Get maestro client
     * @return MaestroClient
     */
    public MaestroClient getClient() {
        return client;
    }
}
