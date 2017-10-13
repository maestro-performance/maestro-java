package net.orpiske.mpt.test.incremental.singlepoint;

import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.maestro.exceptions.MaestroException;
import net.orpiske.mpt.test.SinglePointProfile;
import net.orpiske.mpt.test.incremental.IncrementalTestProfile;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SimpleTestProfile extends IncrementalTestProfile implements SinglePointProfile {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTestProfile.class);

    private String brokerURL;

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public void apply(Maestro maestro) throws MqttException, IOException, MaestroException {
        logger.info("Setting broker to {}", getBrokerURL());
        maestro.setBroker(getBrokerURL());

        logger.info("Setting rate to {}", getRate());
        maestro.setRate(rate);

        logger.info("Rate increment value is {}", getRateIncrement());

        logger.info("Setting parallel count to {}", this.parallelCount);
        maestro.setParallelCount(this.parallelCount);

        logger.info("Parallel count increment value is {}", getParallelCountIncrement());

        logger.info("Setting duration to {}", getDuration());
        maestro.setDuration(this.getDuration().getDuration());

        logger.info("Setting fail-condition-latency to {}", getMaximumLatency());
        maestro.setFCL(getMaximumLatency());

        // Variable message messageSize
        maestro.setMessageSize(getMessageSize());
    }
}
