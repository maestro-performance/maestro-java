package org.maestro.common.inspector.types;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for operating system information
 */
public class OSInfo implements InspectorType, OSInfoType {
    private final Map<String, Object> osProperties;

    public OSInfo(final Map<String, Object> osProperties) {
        this.osProperties = osProperties;
    }

    public Map<String, Object> getOsProperties() {
        return new HashMap<>(osProperties);
    }

    @Override
    public String toString() {
        return "OSInfo{" +
                "osProperties=" + osProperties +
                '}';
    }
}
