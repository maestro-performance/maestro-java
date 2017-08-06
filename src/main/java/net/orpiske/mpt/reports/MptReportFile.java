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

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class MptReportFile extends ReportFile {
    private File rateImg;

    public MptReportFile(File file) {
        super(file);

        // Removes the gz
        String baseName = FilenameUtils.removeExtension(file.getPath());
        // Removes the csv
        baseName = FilenameUtils.removeExtension(baseName);

        rateImg = new File(baseName);
    }
}
