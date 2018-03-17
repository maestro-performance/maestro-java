package org.maestro.common.inspector.types;

import java.util.HashMap;
import java.util.Map;

/**
 * A container class for runtime information
 */
public class RuntimeInformation implements InspectorType {
    private final Map<String, Object> osProperties;

    public RuntimeInformation(final Map<String, Object> osProperties) {
        this.osProperties = osProperties;
    }

    public Map<String, Object> getOsProperties() {
        return new HashMap<>(osProperties);
    }

    @Override
    public String toString() {
        return "RuntimeInformation{" +
                "osProperties=" + osProperties +
                '}';
    }
}
