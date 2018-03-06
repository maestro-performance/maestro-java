package org.maestro.worker.agent;

import org.maestro.client.notes.*;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.worker.base.MaestroWorkerManager;
import org.maestro.worker.ds.MaestroDataServer;

public class MaestroWorkerAgent extends MaestroWorkerManager {
    public MaestroWorkerAgent(String maestroURL, String role, String host, MaestroDataServer dataServer) throws MaestroException {
        super(maestroURL, role, host, dataServer);
    }

    public void handle(StartInspector note) {

    }

    public void handle(StartReceiver note) {

    }

    public void handle(StartSender note) {

    }

    public void handle(StopInspector note) {

    }

    public void handle(StopReceiver note) {

    }

    public void handle(StopSender note) {

    }
}
