package org.maestro.common.agent;

import java.io.File;
import java.util.Map;

public interface AgentHandler {

    void setContext(final Map<String, Object> context);

    void setInitialPath(final File file) throws Exception;

    Object handle(Object input, Object[] args);
}
