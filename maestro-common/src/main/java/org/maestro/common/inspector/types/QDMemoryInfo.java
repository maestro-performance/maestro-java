package org.maestro.common.inspector.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A class for store memory info of SUT
 */
public class QDMemoryInfo implements InspectorType, QDMemoryInfoType {
    private final List<Map<String, Object>> memoryProperties;

    public QDMemoryInfo(final List<Map<String, Object>> queueProperties) {
        this.memoryProperties = Collections.unmodifiableList(queueProperties);
    }

    public List getQDMemoryInfoProperties() {
        return memoryProperties;
    }

    @Override
    public String toString() {
        return "QDMemory{" +
                "productProperties=" + memoryProperties +
                '}';
    }
}
