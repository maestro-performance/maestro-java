package org.maestro.inspector.activemq.writers;

import org.maestro.common.inspector.types.QueueInfo;
import org.maestro.common.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueInfoWriter implements InspectorDataWriter<QueueInfo> {
    private static final Logger logger = LoggerFactory.getLogger(QueueInfoWriter.class);
    @Override
    public void write(QueueInfo data) {
        logger.debug("Queue information: {}", data);
    }
}
