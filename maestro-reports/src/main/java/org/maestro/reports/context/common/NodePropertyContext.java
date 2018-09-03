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

package org.maestro.reports.context.common;

import org.maestro.common.PropertyUtils;
import org.maestro.reports.context.NodeReportContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NodePropertyContext implements NodeReportContext {
    private final Map<String, Object> loadedProperties = new HashMap<>();

    public NodePropertyContext(final String propertiesFile) {
        this(new File(propertiesFile));
    }

    public NodePropertyContext(final File propertiesFile) {
        PropertyUtils.loadProperties(propertiesFile, loadedProperties);
    }

    @Override
    public void eval(final Map<String, Object> context) {
        context.putAll(loadedProperties);
    }
}
