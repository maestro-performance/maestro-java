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

package org.maestro.common.inspector.types;

import java.util.HashMap;
import java.util.Map;

/**
 * A container class for runtime information
 */
public class RuntimeInfo implements InspectorType, RuntimeInfoType {
    private final Map<String, Object> runtimeProperties;

    public RuntimeInfo(final Map<String, Object> runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(runtimeProperties);
    }

    @Override
    public String toString() {
        return "RuntimeInfo{" +
                "runtimeProperties=" + runtimeProperties +
                '}';
    }
}
