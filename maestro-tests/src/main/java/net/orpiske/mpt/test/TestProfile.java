package net.orpiske.mpt.test;

import net.orpiske.mpt.common.exceptions.MaestroException;
import net.orpiske.mpt.maestro.Maestro;

import java.io.IOException;

/**
 * A base interface for implementing test profiles.
 *
 * Test profiles provide a mechanism to tweak test execution parameters. They are used by
 * the test executors. For example, a test executor may use a profile to increase or
 * decrease the rate for a test.
 */
@SuppressWarnings("unused")
public interface TestProfile {

    /**
     * Get the test execution number
     * @return the test execution number
     */
    int getTestExecutionNumber();

    /**
     * Increment the test execution number
     */
    void incrementTestExecutionNumber();

    /**
     * Apply a test profile using a maestro instance
     * @param maestro the maestro instance to apply the profile to
     * @throws IOException I/O and serialization errors
     * @throws MaestroException Incorrect or invalid parameters
     */
    void apply(Maestro maestro) throws MaestroException;
}
