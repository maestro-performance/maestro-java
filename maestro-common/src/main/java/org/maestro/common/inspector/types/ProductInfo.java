package org.maestro.common.inspector.types;

import java.util.HashMap;
import java.util.Map;


/**
 * A container for queue information
 */
public abstract class ProductInfo implements InspectorType, ProductInfoType {
    protected final Map<String, Object> productProperties;

    public ProductInfo(final Map<String, Object> productProperties) {
        this.productProperties = productProperties;
    }

    @Override
    public Map<String, Object> getProductProperties() {
        return new HashMap<>(productProperties);
    }

    @Override
    public String toString() {
        return "QueueInfo{" +
                "productProperties=" + productProperties +
                '}';
    }
}
