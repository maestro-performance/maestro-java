package org.maestro.worker.base;

import org.maestro.client.notes.*;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.worker.ds.MaestroDataServer;

/**
 * A worker manager that is void of workers. It is used for running a standalone data server.
 */
public class VoidWorkerManager extends MaestroWorkerManager {

    public VoidWorkerManager(final String maestroURL, final String role, final String host,
                             final MaestroDataServer dataServer) throws MaestroException
    {
        super(maestroURL, role, host, dataServer);
    }
}
