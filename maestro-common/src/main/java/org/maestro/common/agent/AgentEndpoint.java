package org.maestro.common.agent;

import org.maestro.common.client.notes.MaestroNote;

public interface AgentEndpoint {

    Object handle();

    void setMaestroNote(MaestroNote note);
}
