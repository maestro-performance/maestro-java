package org.maestro.common.agent;

import java.io.File;
import java.util.Map;

/**
 * A handler glues together the maestro broker and the external endpoint.
 */
public interface AgentHandler {

    /**
     * Set of variables to be passed to the endpoint
     * @param context variables as a map
     */
    void setContext(final Map<String, Object> context);

    /**
     * The root path for executing the external endpoint code
     * @param file the initial directory
     * @throws Exception implementation specific
     */
    void setInitialPath(final File file) throws Exception;

    /**
     * Runs all the external point callbacks in an order that is implementation specific
     */
    void runCallbacks();
}
