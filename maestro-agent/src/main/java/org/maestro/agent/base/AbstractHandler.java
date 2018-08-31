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

package org.maestro.agent.base;

import org.maestro.common.agent.AgentEndpoint;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.worker.WorkerOptions;

/**
 * Abstract handler class for external points scripts
 */
public abstract class AbstractHandler implements AgentEndpoint {

    private MaestroClient client;
    private MaestroNote note;
    private WorkerOptions workerOptions;

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


    /**
     * Sets the worker options
     * @param workerOptions the worker options
     */
    public void setWorkerOptions(final WorkerOptions workerOptions) {
        this.workerOptions = workerOptions;
    }


    /**
     * Gets the worker options
     * @return the worker options
     */
    public WorkerOptions getWorkerOptions() {
        return workerOptions;
    }
}
