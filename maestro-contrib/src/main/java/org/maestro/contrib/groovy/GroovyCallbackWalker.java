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

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Recursively adds jars or files to the groovy classpath
 */
public class GroovyCallbackWalker extends DirectoryWalker<File> {

    private static final Logger logger = LoggerFactory.getLogger(GroovyCallbackWalker.class);
    private List<File> fileList;

    public GroovyCallbackWalker() {

    }

    @SuppressWarnings("RedundantThrows")
    @Override
    protected boolean handleDirectory(File directory, int depth, Collection<File> results) throws IOException {
        return true;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    protected void handleFile(File file, int depth, Collection<File> results)
            throws IOException

    {
        logger.debug("Processing file {}", file.getName());

        String ext = FilenameUtils.getExtension(file.getName());

        if (("groovy").equals(ext)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading to the classpath: " + file.getAbsolutePath());
            }

            results.add(file);
        }
    }


    /**
     * The starting directory
     * @param file A file object pointing to the directory
     * @throws IOException for all sorts of I/O errors
     */
    @SuppressWarnings("unchecked")
    public List<File> load(final File file) throws IOException {
        logger.debug("Loading classes from directory {}", file.getPath());

        if (!file.exists()) {
            throw new IOException("The input directory " + file.getPath() + " does not exist");
        }

        try {
            fileList = new ArrayList<>();

            walk(file, fileList);
        } catch (IOException e) {
            logger.error("Unable to walk the whole directory: " + e.getMessage(), e);
            logger.error("Returning a partial list of all the repository data due to errors");
        }

        return fileList;
    }
}