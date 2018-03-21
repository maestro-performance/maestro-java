package org.maestro.inspector.activemq.types;

import org.maestro.common.inspector.types.ProductInfo;
import java.util.Map;

public class ArtemisProductInfo extends ProductInfo {

    public ArtemisProductInfo(Map<String, Object> productProperties) {
        super(productProperties);
    }

    @Override
    public String getProductName() {
        return (String) productProperties.get("Name");
    }

    @Override
    public String getProductVersion() {
        return (String) productProperties.get("Version");
    }


}
