package org.maestro.common.inspector.types;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for operating system information
 */
public class OSInfo implements InspectorType {
    private Map<String, Object> osProperties;

    public OSInfo(Map<String, Object> osProperties) {
        this.osProperties = osProperties;
    }

    public Map<String, Object> getOsProperties() {
        return new HashMap<>(osProperties);
    }

}
