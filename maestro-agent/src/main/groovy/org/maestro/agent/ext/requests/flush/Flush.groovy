package org.maestro.agent.ext.requests.flush

import org.maestro.common.agent.AgentEndpoint

class FlushHandler implements AgentEndpoint{
    @Override
    Object handle(Object input, Object[] args) {
        println "Hello"
        return null
    }
}