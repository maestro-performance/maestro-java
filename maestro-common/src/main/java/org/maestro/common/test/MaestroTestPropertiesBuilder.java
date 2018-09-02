/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.common.test;

import java.io.File;
import java.io.IOException;

/**
 * Build the appropriate MaestroTestProperties file based on the .properties existent on
 * the directory
 */
public class MaestroTestPropertiesBuilder {

    private MaestroTestPropertiesBuilder() {}


    /**
     * Build the MaestroTestProperties object
     * @param directory the directory containing the properties file
     * @return An instance of MaestroTestProperties or null if unable to determine the appropriate
     * @throws IOException on I/O errors (ie.: unable to read the properties file)
     */
    public static MaestroTestProperties build(final File directory) throws IOException {
        final File testPropertiesFile = new File(directory, TestProperties.FILENAME);

        if (testPropertiesFile.exists()) {
            MaestroTestProperties testProperties = new TestProperties();
            testProperties.load(testPropertiesFile);

            return testProperties;
        }

        final File inspectorPropertiesFile = new File(directory, InspectorProperties.FILENAME);
        if (inspectorPropertiesFile.exists()) {
            MaestroTestProperties testProperties = new InspectorProperties();
            testProperties.load(inspectorPropertiesFile);

            return testProperties;
        }

        return null;
    }
}
