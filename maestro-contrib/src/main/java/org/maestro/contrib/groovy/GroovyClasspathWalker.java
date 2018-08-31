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
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Recursively adds jars or files to the groovy classpath
 */
public class GroovyClasspathWalker extends DirectoryWalker<File> {

    private static final Logger logger = LoggerFactory.getLogger(GroovyClasspathWalker.class);

    private final GroovyClassLoader loader;

    public GroovyClasspathWalker(final GroovyClassLoader loader) {
        this.loader = loader;
    }

    @Override
    protected void handleDirectoryStart(File directory, int depth, Collection<File> results) throws IOException {
        loader.addClasspath(directory.getCanonicalPath());
    }

    @Override
    protected void handleFile(File file, int depth, Collection<File> results)
            throws IOException

    {
        String ext = FilenameUtils.getExtension(file.getName());

        if ((".class").equals(ext) || (".jar").equals(ext) || (".zip").equals(ext) ) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading to the classpath: " + file.getAbsolutePath());
            }

            loader.addClasspath(file.getCanonicalPath());
        }
    }


    /**
     * The starting directory
     * @param file A file object pointing to the directory
     */
    @SuppressWarnings("unchecked")
    public void load(final File file) {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading classes from " + file.getName());
        }

        try {
            walk(file, new ArrayList<>());
        } catch (IOException e) {
            logger.error("Unable to walk the whole directory: " + e.getMessage(), e);
            logger.error("Returning a partial list of all the repository data due to errors");
        }
    }
}