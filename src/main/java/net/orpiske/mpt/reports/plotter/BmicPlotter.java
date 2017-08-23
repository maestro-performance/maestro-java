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

package net.orpiske.mpt.reports.plotter;

import net.orpiske.bmic.plot.BmicData;
import net.orpiske.bmic.plot.BmicReader;
import net.orpiske.mpt.reports.MptReportFile;
import net.orpiske.mpt.reports.ReportFile;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

public class BmicPlotter implements Plotter {
    private static final Logger logger = LoggerFactory.getLogger(BmicPlotter.class);
    private BmicReader bmicReader = new BmicReader();

    @Override
    public boolean plot(File file) {
        try {
            BmicData bmicData = bmicReader.read(file.getPath());

            // Removes the gz
            String baseName = FilenameUtils.removeExtension(file.getPath());
            // Removes the csv
            baseName = FilenameUtils.removeExtension(baseName);

            // Plotter
            net.orpiske.bmic.plot.BmicPlotter plotter = new net.orpiske.bmic.plot.BmicPlotter(baseName);
            logger.debug("Number of records to plot: {} ", bmicData.getTimestamps().size());
            for (Date d : bmicData.getTimestamps()) {
                logger.debug("Adding date record for plotting: {}", d);
            }

            plotter.setOutputWidth(1024);
            plotter.setOutputHeight(600);
            plotter.plot(bmicData);

            return true;
        }
        catch (Throwable t) {
            logger.error("Unable to generate report for {}: {}", file.getPath(), t.getMessage());
            logger.trace("Exception: ", t);

            ReportFile reportFile = new MptReportFile(file);
            reportFile.setReportSuccessful(false);
            reportFile.setReportFailure(t);
        }

        return false;

    }
}
