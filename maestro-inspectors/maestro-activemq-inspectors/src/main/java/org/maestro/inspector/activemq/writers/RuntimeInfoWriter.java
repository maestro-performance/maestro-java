package org.maestro.inspector.activemq.writers;


import org.maestro.common.inspector.types.RuntimeInfo;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

public class RuntimeInfoWriter implements InspectorDataWriter<RuntimeInfo> {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeInfoWriter.class);
    private final InspectorProperties inspectorProperties;

    public RuntimeInfoWriter(final InspectorProperties inspectorProperties) {
        this.inspectorProperties = inspectorProperties;
    }

    @Override
    public void write(final LocalDateTime now, final RuntimeInfo data) {
        logger.debug("Runtime information: {}", data);

        Map<String, Object> runtimeProperties = data.getProperties();

        inspectorProperties.setJvmName((String) runtimeProperties.get("VmName"));
        inspectorProperties.setJvmVersion((String) runtimeProperties.get("VmVersion"));
    }
}
