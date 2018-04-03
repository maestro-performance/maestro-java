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
     * @return
     */
    WorkerOptions getWorkerOptions();
}
