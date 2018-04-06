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
 * A container for queue information
 */
public class QueueInfo implements InspectorType, QueueInfoType {
    private final Map<String, Object> queueProperties;

    public QueueInfo(final Map<String, Object> queueProperties) {
        this.queueProperties = queueProperties;
    }

    public Map<String, Object> getQueueProperties() {
        return new HashMap<>(queueProperties);
    }

    @Override
    public String toString() {
        return "QueueInfo{" +
                "productProperties=" + queueProperties +
                '}';
    }
}
