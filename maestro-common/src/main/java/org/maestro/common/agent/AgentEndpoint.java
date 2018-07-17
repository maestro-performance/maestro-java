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

import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.worker.WorkerOptions;

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
     * Get the content of the note associated with the external endpoint.
     * @return the note
     */
    MaestroNote getNote();

    /**
     * Sets the content of the client associated with the external endpoint.
     * @param client the maestro client
     */
    void setMaestroClient(MaestroClient client);

    /**
     * Get the content of the client associated with the external endpoint.
     * @return the maestro client
     */
    MaestroClient getClient();


    /**
     * Sets the worker options
     * @param workerOptions the worker options
     */
    void setWorkerOptions(final WorkerOptions workerOptions);


    /**
     * Gets the worker options
     * @return the worker options object
     */
    WorkerOptions getWorkerOptions();
}
