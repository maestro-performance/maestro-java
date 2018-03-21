package org.maestro.common.inspector.types;

import java.util.HashMap;
import java.util.Map;

/**
 * A container class for runtime information
 */
public class RuntimeInfo implements InspectorType, RuntimeInfoType {
    private final Map<String, Object> runtimeProperties;

    public RuntimeInfo(final Map<String, Object> runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(runtimeProperties);
    }

    @Override
    public String toString() {
        return "RuntimeInfo{" +
                "runtimeProperties=" + runtimeProperties +
                '}';
    }
}
