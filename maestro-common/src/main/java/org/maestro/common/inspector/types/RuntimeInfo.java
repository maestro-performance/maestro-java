package org.maestro.common.inspector.types;

import java.util.HashMap;
import java.util.Map;

/**
 * A container class for runtime information
 */
public class RuntimeInfo implements InspectorType, RuntimeInfoType {
    private final Map<String, Object> osProperties;

    public RuntimeInfo(final Map<String, Object> osProperties) {
        this.osProperties = osProperties;
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(osProperties);
    }

    @Override
    public String toString() {
        return "RuntimeInfo{" +
                "osProperties=" + osProperties +
                '}';
    }
}
