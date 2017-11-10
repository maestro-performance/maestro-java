package net.orpiske.mpt.common.writers;


/**
 * Converts latency data in pure string format into long.
 */
public class StringLatencyConverter implements LatencyDataConverter {
    @Override
    public long convert(String input) {
        return Long.parseLong(input);
    }
}
