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

import net.orpiske.mdp.plot.RateData;
import net.orpiske.mdp.plot.RateDataProcessor;
import net.orpiske.mdp.plot.RateReader;
import net.orpiske.mpt.reports.MptReportFile;
import net.orpiske.mpt.reports.ReportFile;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

public class RatePlotter implements Plotter {
    private static final Logger logger = LoggerFactory.getLogger(RatePlotter.class);
    private RateDataProcessor rateDataProcessor = new RateDataProcessor();
    private RateReader rateReader = new RateReader(rateDataProcessor);

    @Override
    public boolean plot(File file) {
        try {
            rateReader.read(file.getPath());

            RateData rateData = rateDataProcessor.getRateData();

            // Removes the gz
            String baseName = FilenameUtils.removeExtension(file.getPath());
            // Removes the csv
            baseName = FilenameUtils.removeExtension(baseName);

            // Plotter
            net.orpiske.mdp.plot.RatePlotter plotter = new net.orpiske.mdp.plot.RatePlotter(FilenameUtils.removeExtension(baseName));
            logger.debug("Number of records to plot: {} ", rateData.getRatePeriods().size());
            for (Date d : rateData.getRatePeriods()) {
                logger.debug("Adding date record for plotting: {}", d);
            }

            plotter.setOutputWidth(1024);
            plotter.setOutputHeight(600);
            plotter.plot(rateData.getRatePeriods(), rateData.getRateValues());

            return true;
        }
        catch (Throwable t) {
            logger.error("Unable to generate report for {}", file.getPath());
            logger.error("Exception: ", t);

            ReportFile reportFile = new MptReportFile(file);
            reportFile.setReportSuccessful(false);
            reportFile.setReportFailure(t);
        }

        return false;
    }
}
