/*
 * Copyright 2013 Otavio Rodolfo Piske
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.maestro.contrib.groovy;


import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to help managing the groovy classpath
 */
public class GroovyClasspathHelper {

    private static final Logger logger
            = LoggerFactory.getLogger(GroovyClasspathHelper.class);


    private static GroovyClasspathHelper instance;

    private final GroovyClassLoader loader;

    private GroovyClasspathHelper() {
        loader = new GroovyClassLoader(getClass().getClassLoader());
    }


    /**
     * Adds a path to the classpath
     * @param path the path
     */
    public void addClasspath(final String path) {
        logger.trace("Adding path {} to classpath", path);
        loader.addClasspath(path);
    }


    /**
     * Adds a file or directory to the classpath
     * @param file the file object pointing to the file or directory
     * @throws IOException for all sorts of I/O errors
     */
    public void addClasspath(final File file) throws IOException {
        if (file.exists()) {
            logger.trace("Adding path {} to classpath", file.getPath());
            loader.addClasspath(file.getCanonicalPath());
        }
        else {
            logger.trace("Path {} does not exist. Not adding to the classpath", file.getPath());
        }
    }


    /**
     * Gets the Groovy class loader instance
     * @return the Groovy class loader instance
     */
    public GroovyClassLoader getLoader() {
        return loader;
    }


    /**
     * Gets the classpath helper instance
     * @return the Groovy class loader instance
     */
    public static GroovyClasspathHelper getInstance() {
        if (instance == null) {
            instance = new GroovyClasspathHelper();
        }

        return instance;
    }
}