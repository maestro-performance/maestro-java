package org.maestro.common.inspector.types;

import java.util.Map;


/**
 * Java and other runtime information
 */
public interface RuntimeInfoType {
    /**
     * Get OS properties as a map
     * @return
     */
    Map<String, Object> getOsProperties();
}
