package org.maestro.agent.ext.requests.halt

import org.maestro.client.exchange.MaestroTopics
import org.maestro.client.notes.OkResponse
import org.maestro.common.agent.AgentEndpoint
import org.maestro.common.client.MaestroClient
import org.maestro.common.client.notes.MaestroNote

class FlushHandler implements AgentEndpoint{

    private MaestroClient client;

    @Override
    Object handle() {
        client.publish(MaestroTopics.MAESTRO_TOPIC, new OkResponse());
        return null
    }

    @Override
    void setMaestroNote(MaestroNote note) {

    }

    @Override
    void setMaestroClient(MaestroClient client) {
        this.client = client
    }
}