package org.maestro.common.inspector.types;

import java.util.Map;


/**
 * OS information
 */
public interface OSInfoType {
    /**
     * Get the OS properties as a map
     * @return
     */
    Map<String, Object> getOsProperties();
}
