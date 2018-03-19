package org.maestro.inspector.activemq.writers;

import org.maestro.common.inspector.types.OSInfo;
import org.maestro.common.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSInfoWriter implements InspectorDataWriter<OSInfo> {
    private static final Logger logger = LoggerFactory.getLogger(OSInfoWriter.class);

    @Override
    public void write(OSInfo data) {
        logger.debug("Operating system: {}", data);
    }
}
