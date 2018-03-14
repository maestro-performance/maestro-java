package org.maestro.inspector.base;

import org.maestro.client.notes.*;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.worker.base.MaestroWorkerManager;
import org.maestro.worker.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class InspectorManager extends MaestroWorkerManager {
    private static final Logger logger = LoggerFactory.getLogger(InspectorManager.class);
    private static final String INSPECTOR_ROLE = "inspector";
    private Thread inspectorThread;
    private InspectorContainer inspectorContainer;

    public InspectorManager(final String maestroURL, final String host, final MaestroDataServer dataServer,
                            final MaestroInspector inspector) throws MaestroException
    {
        super(maestroURL, INSPECTOR_ROLE, host, dataServer);

        inspector.setUrl("http://localhost:8161/console/jolokia");

        inspectorContainer = new InspectorContainer(inspector);
    }

    public void handle(StartInspector note) {
        logger.debug("Start inspector request received");

        inspectorThread = new Thread(inspectorContainer);

        inspectorThread.run();
    }

    public void handle(StopInspector note) {
        inspectorThread.interrupt();
    }

    public void handle(StartReceiver note) {
        // NO-OP
    }

    public void handle(StartSender note) {
        // NO-OP
    }

    public void handle(StopReceiver note) {
        // NO-OP
    }

    public void handle(StopSender note) {
        // NO-P
    }
}
