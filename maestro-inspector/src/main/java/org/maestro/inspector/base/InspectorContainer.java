package org.maestro.inspector.base;

import org.maestro.common.inspector.MaestroInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InspectorContainer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(InspectorContainer.class);
    private final MaestroInspector inspector;

    public InspectorContainer(final MaestroInspector inspector) {
        this.inspector = inspector;
    }


    public void run() {
        try {
            inspector.start();
        }
        catch (InterruptedException e) {

        }
        catch (Exception e) {
            logger.error("Error running the inspector: {}", e.getMessage(), e);
        }
    }
}
