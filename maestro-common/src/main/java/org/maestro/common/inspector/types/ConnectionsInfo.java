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

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * A container for connections information
 */
public class ConnectionsInfo implements InspectorType, ConnectionsInfoType {
    private final List<Map<String, Object>> connectionProperties;

    public ConnectionsInfo(final List<Map<String, Object>> queueProperties) {
        this.connectionProperties = Collections.unmodifiableList(queueProperties);
    }

    public List<Map<String, Object>> getConnectionProperties() {
        return connectionProperties;
    }

    @Override
    public String toString() {
        return "ConnectionsInfo{" +
                "productProperties=" + connectionProperties +
                '}';
    }
}
