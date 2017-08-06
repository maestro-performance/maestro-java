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

public class HdrHistogramReportFile extends ReportFile {
    private File percentileImgAll;
    private File percentileImg90;
    private File percentileImg99;

    public HdrHistogramReportFile(File file) {
        super(file);

        String basename = FilenameUtils.removeExtension(file.getPath());

        percentileImgAll = new File(basename + "_all.png");
        percentileImg90 = new File(basename + "_90.png");
        percentileImg99 = new File(basename + "_99.png");
    }

}
