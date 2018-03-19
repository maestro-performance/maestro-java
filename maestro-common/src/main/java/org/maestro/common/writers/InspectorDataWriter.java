package org.maestro.common.writers;


import org.maestro.common.inspector.types.InspectorType;

public interface InspectorDataWriter<T extends InspectorType> {
    void write(T data);
}
