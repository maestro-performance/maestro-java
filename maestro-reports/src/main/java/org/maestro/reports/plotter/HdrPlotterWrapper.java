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


import org.HdrHistogram.Histogram;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.test.TestProperties;
import org.maestro.common.worker.WorkerUtils;
import org.maestro.plotter.latency.HdrLogProcessorWrapper;
import org.maestro.plotter.latency.common.HdrData;
import org.maestro.plotter.latency.graph.HdrPlotter;
import org.maestro.plotter.latency.properties.HdrPropertyWriter;
import org.maestro.plotter.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class HdrPlotterWrapper implements PlotterWrapper {
    private static final Logger logger = LoggerFactory.getLogger(HdrPlotterWrapper.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private static final double DEFAULT_UNIT_RATE;
    private static final boolean legacyHdrMode;

    private double unitRate;

    static {
        DEFAULT_UNIT_RATE = config.getDouble("hdr.plotter.default.unit.rate", 1.0);
        legacyHdrMode = config.getBoolean("hdr.plotter.legacy.mode", false);
    }

    public HdrPlotterWrapper() {
        this(DEFAULT_UNIT_RATE);
    }

    public HdrPlotterWrapper(double unitRate) {
        this.unitRate = unitRate;
    }

    private TestProperties loadProperties(final File baseDir) {
        final File propertiesFile = new File(baseDir, "test.properties");

        if (propertiesFile.exists()) {
            logger.debug("Loading test.properties file to check for bounded/unbounded rate");
            TestProperties testProperties = new TestProperties();

            try {
                testProperties.load(propertiesFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return testProperties;
        }

        return null;
    }

    @Override
    public boolean plot(final File file) {

        logger.debug("Plotting HDR file {}", file.getPath());

        try {
            if (!file.exists()) {
                throw new IOException("File " + file.getPath() + " does not exist");
            }

            final Histogram histogram = Util.getAccumulated(file);

            final HdrData hdrData = getHdrData(histogram, file);

            // HdrPlotterWrapper
            HdrPlotter plotter = new HdrPlotter(FilenameUtils.removeExtension(file.getName()));

            plotter.plot(hdrData, file.getParentFile());

            HdrPropertyWriter propertyWriter = new HdrPropertyWriter();

            propertyWriter.postProcess(histogram, file);

            return true;
        }
        catch (Exception t) {
            handlePlotException(file, t);
            throw new MaestroException(t);
        }
    }

    private synchronized HdrData getHdrData(final Histogram histogram, final File file) {
        HdrData hdrData;
        if (!legacyHdrMode) {
            TestProperties testProperties = loadProperties(file.getParentFile());
            if (testProperties != null) {
                final long intervalInNanos = WorkerUtils.getExchangeInterval(testProperties.getRate());

                if (intervalInNanos == 0) {
                    hdrData = getHdrDataUnbounded(histogram);
                } else {
                    hdrData = getHdrDataBounded(histogram, intervalInNanos);
                }
            } else {
                hdrData = getHdrDataUnbounded(histogram);
            }
        }
        else {
            hdrData = getHdrDataUnbounded(histogram);
        }
        return hdrData;
    }

    private HdrData getHdrDataUnbounded(final Histogram histogram) {
        final HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper(unitRate);


        return processorWrapper.convertLog(histogram);
    }


    private HdrData getHdrDataBounded(final Histogram histogram, final long interval) {
        final HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper(unitRate);

        return processorWrapper.convertLog(histogram, interval);
    }
}
