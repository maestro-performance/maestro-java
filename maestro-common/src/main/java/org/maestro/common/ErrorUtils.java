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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Utility code for test errors
 */
public class ErrorUtils {
    private static final Logger logger = LoggerFactory.getLogger(ErrorUtils.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private static final List<String> ignoredErrors;

    static {
        String[] errors = config.getStringArray("ignored.errors");
        ignoredErrors = Arrays.asList(errors);
    }

    private ErrorUtils() {}

    /**
     * Checks whether the message returned by an exception matches with known ignored errors on the configuration
     * @param peerName pretty peer name that reported the error
     * @param notificationMessage the notification message to check
     * @return true if it is ignored or false otherwise
     */
    public static boolean isIgnored(final String peerName, final String notificationMessage) {
        for (String message : ignoredErrors) {
            if (notificationMessage.trim().equals(message.trim())) {
                logger.warn("The test failed on {} but the error ({}) is being ignored. This is " +
                                "likely to be caused by a known problem in a software under test",
                        peerName, notificationMessage);

                return true;
            }
        }

        return false;
    }



}
