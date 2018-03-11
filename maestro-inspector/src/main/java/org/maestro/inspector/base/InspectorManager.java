package org.maestro.inspector.base;

import org.maestro.client.notes.*;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.worker.base.MaestroWorkerManager;
import org.maestro.worker.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InspectorManager extends MaestroWorkerManager {
    private static final Logger logger = LoggerFactory.getLogger(InspectorManager.class);
    private Thread inspectorThread;
    private InspectorContainer inspectorContainer;

    public InspectorManager(final String maestroURL, final String role, final String host,
                            final MaestroDataServer dataServer, final MaestroInspector inspector) throws MaestroException
    {
        super(maestroURL, role, host, dataServer);

        inspectorContainer = new InspectorContainer(inspector);
    }

    public void handle(StartInspector note) {

    }

    public void handle(StartReceiver note) {

    }

    public void handle(StartSender note) {

    }

    public void handle(StopInspector note) {
        logger.debug("Start inspector request received");

        inspectorThread = new Thread(inspectorContainer);

        inspectorThread.run();
    }

    public void handle(StopReceiver note) {
        inspectorThread.interrupt();
    }

    public void handle(StopSender note) {

    }
}
