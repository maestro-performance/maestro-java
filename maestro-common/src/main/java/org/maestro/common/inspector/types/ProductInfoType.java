package org.maestro.common.inspector.types;

import java.util.Map;

/**
 * A container for product information
 */
public interface ProductInfoType {
    String getProductName();

    String getProductVersion();

    Map<String, Object> getProductProperties();
}
