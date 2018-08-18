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

import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.common.BasicPlotter;
import org.maestro.plotter.common.ReportReader;
import org.maestro.plotter.rate.RateDataReader;
import org.maestro.plotter.rate.graph.RatePlotter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RatePlotterWrapper implements PlotterWrapper {
    private static final Logger logger = LoggerFactory.getLogger(RatePlotterWrapper.class);

    @Override
    public boolean plot(final File file) throws MaestroException {
        ReportReader<?> rateReader = null;

        if (file.getName().endsWith("dat")) {
            rateReader = new RateDataReader();
        }


        logger.debug("Plotting Maestro rate file {}", file.getPath());

        BasicPlotter<? extends ReportReader<?>, RatePlotter> basicPlotter = new BasicPlotter<>(rateReader, new RatePlotter());

        File propertiesFile = new File(file.getParentFile(), "rate.properties");
        File outputFile = new File(file.getParentFile(), "rate.png");

        basicPlotter.plot(file, outputFile, propertiesFile);

        return true;
    }
}
