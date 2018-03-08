package org.maestro.agent.ext.requests.flush

import org.maestro.common.agent.AgentEndpoint
import org.maestro.common.client.notes.MaestroNote

class FlushHandler implements AgentEndpoint{
    @Override
    Object handle() {
        println "Hello"
        return null
    }

    @Override
    void setMaestroNote(MaestroNote note) {

    }
}