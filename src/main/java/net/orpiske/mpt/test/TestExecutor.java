package net.orpiske.mpt.test;

/**
 * A base interface for executing tests
 */
public interface TestExecutor {

    /**
     * Runs the test
     * @return true if successful or false otherwise
     */
    boolean run();
}
