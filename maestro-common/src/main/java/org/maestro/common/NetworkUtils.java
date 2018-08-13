/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
     * @return The hostname
     * @throws UnknownHostException if the hostname is invalid or cannot be resolved
     */
    public static String getHost(final String option) throws UnknownHostException {
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
