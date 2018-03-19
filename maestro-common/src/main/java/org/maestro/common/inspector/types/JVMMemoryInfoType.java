package org.maestro.common.inspector.types;

/**
 * JVM memory information
 */
public interface JVMMemoryInfoType {

    /**
     * The name of the JVM memory area (ie.: heap, tenured, old gen, etc)
     * @return the name of the JVM memory area
     */
    String getMemoryAreaName();

    /**
     * The initial size for the memory area
     * @return the initial number of bytes
     */
    long getInitial();

    /**
     * The committed size for the memory area
     * @return the committed number of bytes
     */
    long getCommitted();

    /**
     * The maximum size for the memory area
     * @return the maximum number of bytes
     */
    long getMax();


    /**
     * Currently used memory for the memory area
     * @return the number of bytes used
     */
    long getUsed();
}
