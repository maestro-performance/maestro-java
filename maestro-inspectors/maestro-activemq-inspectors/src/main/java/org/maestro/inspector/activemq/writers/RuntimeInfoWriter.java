package org.maestro.inspector.activemq.writers;


import org.maestro.common.inspector.types.RuntimeInfo;
import org.maestro.common.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeInfoWriter implements InspectorDataWriter<RuntimeInfo> {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeInfoWriter.class);

    @Override
    public void write(RuntimeInfo data) {
        logger.debug("Runtime information: {}", data);
    }
}
