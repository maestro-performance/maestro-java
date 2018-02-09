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

package org.maestro.reports.files;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class MptReportFile extends ReportFile {
    private final File rateImg;

    public MptReportFile(File sourceFile, File normalizedFile) {
        super(sourceFile, normalizedFile);

        // Removes the gz
        String baseName = FilenameUtils.removeExtension(normalizedFile.getPath());
        // Removes the csv
        baseName = FilenameUtils.removeExtension(baseName);

        rateImg = new File(baseName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MptReportFile that = (MptReportFile) o;

        return rateImg != null ? rateImg.equals(that.rateImg) : that.rateImg == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (rateImg != null ? rateImg.hashCode() : 0);
        return result;
    }
}
