package org.maestro.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stale checker that checks if a count is non-progressing (ie.: if after a given number of retries
 * the count is still the same). It assumes that the checked value is ever increasing (ie.: a sum of
 * all messages sent)
 */
public class NonProgressingStaleChecker implements StaleChecker {
    private static final Logger logger = LoggerFactory.getLogger(NonProgressingStaleChecker.class);

    private final long retries;
    private long lastCount = 0;
    private long repeat = 0;

    /**
     * Constructor
     * @param retries the number of retries to do before considering the count as stale
     */
    public NonProgressingStaleChecker(long retries) {
        this.retries = retries;
    }

    public boolean isStale(long count) {
        if (count > lastCount) {
            lastCount = count;
        }
        else {
            if (count == lastCount) {
                repeat++;
                logger.trace("Current count is the same as last count. Checking if stale");

                if (repeat >= retries) {
                    logger.trace("Count is stale");

                    return true;
                }
            }
        }

        return false;
    }
}
