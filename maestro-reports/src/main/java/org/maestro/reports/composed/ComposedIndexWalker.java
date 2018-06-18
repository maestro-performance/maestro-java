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

package org.maestro.reports.composed;

import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Walks through the report directory in order to build the list of files to process
 */
final class ComposedIndexWalker extends DirectoryWalker<ComposedProperties> {
    private static final Logger logger = LoggerFactory.getLogger(ComposedIndexWalker.class);

    public ComposedIndexWalker() {
    }


    @Override
    protected void handleFile(final File file, int depth, Collection<ComposedProperties> results) {
        if (file.getName().equals("index.properties")) {
            logger.trace("Adding file {}", file.getPath());
            results.add(new ComposedProperties(file));
        }
    }

    List<ComposedProperties> generate(final File baseDir) {
        List<ComposedProperties> files = new LinkedList<>();

        if (logger.isDebugEnabled()) {
            logger.debug("Processing composed reports on {}", baseDir.getName());
        }

        try {
           if (baseDir.exists()) {
                walk(baseDir, files);
            }
            else {
                logger.error("The base directory does not exist: {}", baseDir.getPath());
            }
        } catch (IOException e) {
            logger.error("Unable to walk the whole directory: {}", e.getMessage(), e);
            logger.error("Returning a partial list of all the reports due to errors");
        }

        return files;
    }
}
