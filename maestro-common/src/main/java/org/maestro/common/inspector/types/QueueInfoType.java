package org.maestro.common.inspector.types;

import java.util.Map;


/**
 * Information about the queues in the SUT
 */
public interface QueueInfoType {
    /**
     * Get the queue properties
     * @return
     */
    Map<String, Object> getQueueProperties();
}
