package net.orpiske.mpt.test;

import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.maestro.exceptions.MaestroException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

/**
 * A base interface for implementing test profiles.
 *
 * Test profiles provide a mechanism to tweak test execution parameters. They are used by
 * the test executors. For example, a test executor may use a profile to increase or
 * decrease the rate for a test.
 */
public interface TestProfile {

    /**
     * Apply a test profile using a maestro instance
     * @param maestro the maestro instance to apply the profile to
     * @throws MqttException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     * @throws MaestroException Incorrect or invalid parameters
     */
    void apply(Maestro maestro) throws MqttException, IOException, MaestroException;
}
