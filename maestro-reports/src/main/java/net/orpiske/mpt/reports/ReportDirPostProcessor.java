/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.orpiske.mpt.reports;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ReportDirPostProcessor extends DirectoryWalker {
    private static Logger logger = LoggerFactory.getLogger(ReportDirPostProcessor.class);
    private String initialPath;

    private List<ReportFile> files = new LinkedList<>();

    public ReportDirPostProcessor(String initialPath) {
        this.initialPath = initialPath;
    }

    @Override
    protected void handleFile(File file, int depth, Collection results)
            throws IOException

    {
        logger.debug("Post processing file {}", file.getPath());
        String ext = FilenameUtils.getExtension(file.getName());

        switch (ext) {
            case "hdr":
            case "gz":
            case "csv":
                FileUtils.deleteQuietly(file);
            default:

        }
    }

    @SuppressWarnings("unchecked")
    public List<ReportFile> postProcess(final File reportsDir) {

        if (logger.isDebugEnabled()) {
            logger.debug("Post processing downloaded reports on {}", reportsDir.getName());
        }

        try {
           if (reportsDir.exists()) {
                walk(reportsDir, new ArrayList());
            }
            else {
                logger.error("The reports directory does not exist: {}", reportsDir.getPath());
            }
        } catch (IOException e) {
            logger.error("Unable to walk the whole directory: " + e.getMessage(), e);
            logger.error("Returning a partial list of all the reports due to errors");
        }

        return files;
    }
}
