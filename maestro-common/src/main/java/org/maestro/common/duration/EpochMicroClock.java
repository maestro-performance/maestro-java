package org.maestro.common.duration;

@FunctionalInterface
public interface EpochMicroClock {
    /**
     * Time in microseconds since 1 Jan 1970 UTC.
     *
     * @return the number of microseconds since 1 Jan 1970 UTC.
     */
    long microTime();
}