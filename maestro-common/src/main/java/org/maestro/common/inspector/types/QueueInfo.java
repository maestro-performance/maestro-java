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
                "queueProperties=" + queueProperties +
                '}';
    }
}
