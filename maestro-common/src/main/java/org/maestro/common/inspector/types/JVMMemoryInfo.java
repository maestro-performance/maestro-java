package org.maestro.common.inspector.types;

public class JVMMemoryInfo {
    private final String memoryAreaName;
    private long initial;
    private long committed;
    private long max;
    private long used;

    public JVMMemoryInfo(final String memoryAreaName, long initial, long committed, long max, long used) {
        this.memoryAreaName = memoryAreaName;
        this.initial = initial;
        this.committed = committed;
        this.max = max;
        this.used = used;
    }

    public String getMemoryAreaName() {
        return memoryAreaName;
    }

    public long getInitial() {
        return initial;
    }

    public long getCommitted() {
        return committed;
    }

    public long getMax() {
        return max;
    }

    public long getUsed() {
        return used;
    }

    @Override
    public String toString() {
        return "JVMMemoryInfo{" +
                "memoryAreaName='" + memoryAreaName + '\'' +
                ", initial=" + initial +
                ", committed=" + committed +
                ", max=" + max +
                ", used=" + used +
                '}';
    }
}
