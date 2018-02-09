package org.maestro.tests;

/**
 * A base interface for executing tests
 */
public interface TestExecutor {

    /**
     * Runs the test
     * @return true if successful or false otherwise
     */
    boolean run();

    /**
     * Gets the amount of time (in milliseconds) to wait for the SUT to cool down
     * @return
     */
    long getCoolDownPeriod();

    /**
     * Sets the amount of time (in milliseconds) to wait for the SUT to cool down
     * @param period
     */
    void setCoolDownPeriod(long period);
}
