package org.maestro.common.agent;

public interface AgentEndpoint {

    Object handle(Object input, Object[] args);
}
