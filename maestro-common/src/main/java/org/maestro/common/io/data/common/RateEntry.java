package org.maestro.common.io.data.common;

public class RateEntry {
    public static final int BYTES = Integer.BYTES + Long.BYTES + Long.BYTES;

    private int metadata;
    private long count;
    private long timestamp;

    public RateEntry(int metadata, long count, long timestamp) {
        this.metadata = metadata;
        this.count = count;
        this.timestamp = timestamp;
    }

    public int getMetadata() {
        return metadata;
    }

    public long getCount() {
        return count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "RateEntry{" +
                "metadata=" + metadata +
                ", count=" + count +
                ", timestamp=" + timestamp +
                '}';
    }
}
