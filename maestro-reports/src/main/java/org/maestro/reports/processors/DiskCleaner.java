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

package org.maestro.reports.processors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.maestro.common.Constants;
import org.maestro.reports.files.ReportFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class DiskCleaner implements ReportFileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DiskCleaner.class);
    private static final String FILE_EXTENSION_CSV = "csv";

    private void clean(ReportFile reportFile) {
        File sourceFile = reportFile.getSourceFile();

        String ext = FilenameUtils.getExtension(sourceFile.getName());

        switch (ext) {
            case Constants.FILE_EXTENSION_HDR_HISTOGRAM:
            case Constants.FILE_EXTENSION_MAESTRO:
            case FILE_EXTENSION_CSV:
                logger.debug("Cleaning file {}", sourceFile.getPath());
                FileUtils.deleteQuietly(sourceFile);
                break;
            default:
                break;
        }
    }

    @Override
    public void process(List<ReportFile> reportFiles) {
        reportFiles.forEach(this::clean);
    }
}
