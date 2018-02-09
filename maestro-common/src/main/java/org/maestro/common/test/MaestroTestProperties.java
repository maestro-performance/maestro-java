package org.maestro.common.test;

import java.io.File;
import java.io.IOException;

/**
 * The common interface for handling properties saved by peers and
 * read by front-ends
 */
public interface MaestroTestProperties {

    /**
     * Load a properties file (ie.: test.properties)
     * @param testProperties A file object pointing to the file to be loaded
     * @throws IOException If the file cannot be read
     */
    void load(File testProperties) throws IOException;

    /**
     * Write to a properties file
     * @param testProperties A file object pointing to the file to be written
     * @throws IOException If the file cannot be written
     */
    void write(File testProperties) throws IOException;
}
