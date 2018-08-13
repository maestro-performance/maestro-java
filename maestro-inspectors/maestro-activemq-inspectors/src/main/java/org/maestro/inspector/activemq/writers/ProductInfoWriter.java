/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.inspector.activemq.writers;

import org.maestro.common.inspector.types.ProductInfo;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.io.data.writers.InspectorDataWriter;
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
