package org.maestro.common.writers;


import org.maestro.common.inspector.types.InspectorType;

import java.time.LocalDateTime;

public interface InspectorDataWriter<T extends InspectorType> {
    void write(LocalDateTime time, T data);
}
