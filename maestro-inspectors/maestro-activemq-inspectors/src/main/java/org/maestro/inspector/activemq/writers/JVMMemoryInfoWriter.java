package org.maestro.inspector.activemq.writers;

import org.maestro.common.inspector.types.JVMMemoryInfo;
import org.maestro.common.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JVMMemoryInfoWriter implements InspectorDataWriter<JVMMemoryInfo> {
    private static final Logger logger = LoggerFactory.getLogger(JVMMemoryInfoWriter.class);
    @Override
    public void write(JVMMemoryInfo data) {
        logger.debug("{} Memory Usage: {}", data.getMemoryAreaName(), data);
    }
}
