/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
