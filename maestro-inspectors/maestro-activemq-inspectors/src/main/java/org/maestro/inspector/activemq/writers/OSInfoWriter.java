package org.maestro.inspector.activemq.writers;

import org.maestro.common.inspector.types.OSInfo;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

public class OSInfoWriter implements InspectorDataWriter<OSInfo> {
    private static final Logger logger = LoggerFactory.getLogger(OSInfoWriter.class);
    private final InspectorProperties inspectorProperties;

    public OSInfoWriter(final InspectorProperties inspectorProperties) {
        this.inspectorProperties = inspectorProperties;
    }

    @Override
    public void write(final LocalDateTime now, final OSInfo data) {
        logger.debug("Operating system: {}", data);

        Map<String, Object> osProperties = data.getOsProperties();

        inspectorProperties.setOperatingSystemArch((String) osProperties.get("Arch"));
        inspectorProperties.setOperatingSystemName((String) osProperties.get("Name"));
        inspectorProperties.setOperatingSystemVersion((String) osProperties.get("Version"));

        inspectorProperties.setSystemCpuCount((Long) osProperties.get("AvailableProcessors"));
        inspectorProperties.setSystemMemory((Long) osProperties.get("TotalPhysicalMemorySize"));
        inspectorProperties.setSystemSwap((Long) osProperties.get("TotalSwapSpaceSize"));
    }
}
