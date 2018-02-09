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

public class HdrHistogramReportFile extends ReportFile {
    private final File percentileImgAll;
    private final File percentileImg90;
    private final File percentileImg99;

    public HdrHistogramReportFile(final File sourceFile, final File normalizedFile) {
        super(sourceFile, normalizedFile);

        String basename = FilenameUtils.removeExtension(normalizedFile.getPath());

        percentileImgAll = new File(basename + "_all.png");
        percentileImg90 = new File(basename + "_90.png");
        percentileImg99 = new File(basename + "_99.png");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HdrHistogramReportFile that = (HdrHistogramReportFile) o;

        if (percentileImgAll != null ? !percentileImgAll.equals(that.percentileImgAll) : that.percentileImgAll != null)
            return false;
        if (percentileImg90 != null ? !percentileImg90.equals(that.percentileImg90) : that.percentileImg90 != null)
            return false;
        return percentileImg99 != null ? percentileImg99.equals(that.percentileImg99) : that.percentileImg99 == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (percentileImgAll != null ? percentileImgAll.hashCode() : 0);
        result = 31 * result + (percentileImg90 != null ? percentileImg90.hashCode() : 0);
        result = 31 * result + (percentileImg99 != null ? percentileImg99.hashCode() : 0);
        return result;
    }
}
