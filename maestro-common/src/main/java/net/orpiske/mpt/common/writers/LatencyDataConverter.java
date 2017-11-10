package net.orpiske.mpt.common.writers;

/**
 * A data converter interface for converting latency data
 */
public interface LatencyDataConverter {

    /**
     * Converts the input string
     * @param input
     * @return a long that represents the latency in milliseconds
     */
    long convert(final String input);
}
