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

import net.orpiske.hhp.plot.HdrData;
import net.orpiske.hhp.plot.HdrLogProcessorWrapper;
import net.orpiske.hhp.plot.HdrPropertyWriter;
import net.orpiske.hhp.plot.HdrReader;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.test.TestProperties;
import org.maestro.common.worker.WorkerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class HdrPlotterWrapper implements PlotterWrapper {
    private static final Logger logger = LoggerFactory.getLogger(HdrPlotterWrapper.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private static final String DEFAULT_UNIT_RATE;
    private static final boolean legacyHdrMode;

    private String unitRate;

    static {
        DEFAULT_UNIT_RATE = config.getString("hdr.plotter.default.unit.rate", "1000");
        legacyHdrMode = config.getBoolean("hdr.plotter.legacy.mode", false);
    }

    public HdrPlotterWrapper() {
        this(DEFAULT_UNIT_RATE);
    }

    public HdrPlotterWrapper(final String unitRate) {
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

            final HdrData hdrData = getHdrData(file);

            // HdrPlotterWrapper
            net.orpiske.hhp.plot.HdrPlotter plotter = new net.orpiske.hhp.plot.HdrPlotter(FilenameUtils.removeExtension(file.getPath()));

            plotter.getChartProperties().setTitle("");
            plotter.getChartProperties().setSeriesName("Percentile");
            plotter.setOutputWidth(1280);
            plotter.setOutputHeight(1024);
            plotter.plot(hdrData);

            HdrPropertyWriter propertyWriter = new HdrPropertyWriter();

            propertyWriter.postProcess(file);

            return true;
        }
        catch (Exception t) {
            handlePlotException(file, t);
            throw new MaestroException(t);
        }
    }

    private HdrData getHdrData(File file) throws IOException {
        HdrData hdrData;
        if (!legacyHdrMode) {
            TestProperties testProperties = loadProperties(file.getParentFile());
            if (testProperties != null) {
                final long intervalInNanos = WorkerUtils.getExchangeInterval(testProperties.getRate());

                if (intervalInNanos == 0) {
                    hdrData = getHdrDataUnbounded(file);
                } else {
                    hdrData = getHdrDataBounded(file, intervalInNanos);
                }
            } else {
                hdrData = getHdrDataUnbounded(file);
            }
        }
        else {
            hdrData = getHdrDataUnbounded(file);
        }
        return hdrData;
    }

    private HdrData getHdrDataUnbounded(File file) throws IOException {
        final HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper(unitRate);
        String csvFile;

        synchronized (this) {
            csvFile = processorWrapper.convertLog(file.getPath());
        }

        // CSV Reader
        final HdrReader reader = new HdrReader();
        return reader.read(csvFile);
    }


    private HdrData getHdrDataBounded(File file, final long interval) throws IOException {
        final HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper(unitRate);
        String[] csvFile;

        synchronized (this) {
            csvFile = processorWrapper.convertLog(file.getPath(), String.valueOf(interval));
        }

        // CSV Reader
        final HdrReader reader = new HdrReader();
        return reader.read(csvFile[0], csvFile[1]);
    }
}
