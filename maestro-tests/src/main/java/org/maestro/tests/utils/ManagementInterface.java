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

package org.maestro.tests.utils;

import org.maestro.tests.AbstractTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for the management interface handling during test setup
 */
public class ManagementInterface {
    private static final Logger logger = LoggerFactory.getLogger(ManagementInterface.class);

    private ManagementInterface() {}

    /**
     * Setups the management interface when provided
     * @param managementInterfaceUrl the URL for the management interface
     * @param inspectorName the name of the inspector to use
     * @param testProfile the test profile
     */
    public static void setupInterface(final String managementInterfaceUrl, final String inspectorName,
                               final AbstractTestProfile testProfile)  {
        if (managementInterfaceUrl != null) {
            if (inspectorName != null) {
                testProfile.setInspectorName(inspectorName);
                testProfile.setManagementInterface(managementInterfaceUrl);
            }
            else {
                logger.info("A management interface was provided by no inspector name was given. Ignoring ...");
            }
        }
        else {
            logger.info("No management interface address was given");
        }
    }
}
