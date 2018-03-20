package org.maestro.inspector.activemq.converter;

import java.util.Map;

public class QueueInfoConverter extends MapConverter {
    public QueueInfoConverter(Map<String, Object> properties) {
        super(properties);
    }

    public void convert(final String key, Map<String, Object> queueProperties) {
        getProperties().put(key, queueProperties);
    }
}
