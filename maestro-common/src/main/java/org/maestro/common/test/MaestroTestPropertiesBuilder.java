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
     * @throws IOException
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
