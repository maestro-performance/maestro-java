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


import org.maestro.common.inspector.types.RuntimeInfo;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.io.data.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

public class RuntimeInfoWriter implements InspectorDataWriter<RuntimeInfo> {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeInfoWriter.class);
    private final InspectorProperties inspectorProperties;

    public RuntimeInfoWriter(final InspectorProperties inspectorProperties) {
        this.inspectorProperties = inspectorProperties;
    }

    @Override
    public void write(final LocalDateTime now, final RuntimeInfo data) {
        logger.trace("Runtime information: {}", data);

        Map<String, Object> runtimeProperties = data.getProperties();
        
        inspectorProperties.setJvmName((String) runtimeProperties.get("VmName"));
        inspectorProperties.setJvmVersion((String) runtimeProperties.get("SpecVersion"));
    }
}
