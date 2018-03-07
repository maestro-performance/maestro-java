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

package org.maestro.reports.plotter;

import net.orpiske.bmic.plot.BmicData;
import net.orpiske.bmic.plot.BmicReader;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class BmicPlotterWrapper implements PlotterWrapper {
    private static final Logger logger = LoggerFactory.getLogger(BmicPlotterWrapper.class);
    private final BmicReader bmicReader = new BmicReader();

    @Override
    public boolean plot(final File file) {
        logger.debug("Plotting BMIC file {}", file.getPath());

        // Removes the gz
        String baseName = FilenameUtils.removeExtension(file.getPath());
        // Removes the csv
        baseName = FilenameUtils.removeExtension(baseName);

        try {
            if (!file.exists()) {
                throw new IOException("File " + file.getPath() + " does not exist");
            }

            BmicData bmicData = bmicReader.read(file.getPath());

            // PlotterWrapper
            net.orpiske.bmic.plot.BmicPlotter plotter = new net.orpiske.bmic.plot.BmicPlotter(baseName);
            logger.debug("Number of records to plot: {} ", bmicData.getTimestamps().size());
            for (Date d : bmicData.getTimestamps()) {
                logger.debug("Adding date record for plotting: {}", d);
            }

            plotter.setOutputWidth(1280);
            plotter.setOutputHeight(1024);
            plotter.plot(bmicData);

            return true;
        }
        catch (Throwable t) {
            handlePlotException(file, t);
        }

        return false;

    }
}
