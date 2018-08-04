package org.maestro.common;

/**
 * Checks if a count is stale
 */
public interface StaleChecker {

    /**
     * Checks if some count is stale
     * @param count the count to check
     * @return true if it's stale or false otherwise
     */
    boolean isStale(long count);
}
