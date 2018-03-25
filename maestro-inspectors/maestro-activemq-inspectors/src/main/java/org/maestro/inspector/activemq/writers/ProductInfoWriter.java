package org.maestro.inspector.activemq.writers;

import org.maestro.common.inspector.types.ProductInfo;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class ProductInfoWriter implements InspectorDataWriter<ProductInfo> {
    private static final Logger logger = LoggerFactory.getLogger(ProductInfoWriter.class);
    private final InspectorProperties inspectorProperties;

    public ProductInfoWriter(final InspectorProperties inspectorProperties) {
        this.inspectorProperties = inspectorProperties;
    }

    @Override
    public void write(LocalDateTime time, ProductInfo data) {
        logger.trace("Writing product information: {}", data);

        inspectorProperties.setProductName(data.getProductName());
        inspectorProperties.setProductVersion(data.getProductVersion());
    }
}
