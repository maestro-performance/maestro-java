package org.maestro.common.inspector.types;

public class JVMHeapInfo {
    private long initial;
    private long committed;
    private long max;
    private long used;

    public JVMHeapInfo(long initial, long committed, long max, long used) {
        this.initial = initial;
        this.committed = committed;
        this.max = max;
        this.used = used;
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
        return "JVMHeapInfo{" +
                "initial=" + initial +
                ", committed=" + committed +
                ", max=" + max +
                ", used=" + used +
                '}';
    }
}
