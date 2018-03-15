package org.maestro.common;

import org.apache.commons.configuration.AbstractConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Network utilities
 */
public class NetworkUtils {
    public static final String MAESTRO_HOST_PROPERTY = "maestro.host";
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    /**
     * Gets the hostname
     * @param option An optional configuration option used to override auto-detection
     * @return
     * @throws UnknownHostException
     */
    public static final String getHost(final String option) throws UnknownHostException {
        String host = System.getProperty(MAESTRO_HOST_PROPERTY);

        if (host != null) {
            return host;
        }

        if (option != null) {
            host = config.getString(option);
            if (host != null) {
                return host;
            }
        }

        return InetAddress.getLocalHost().getHostName();
    }
}
