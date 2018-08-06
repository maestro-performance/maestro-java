/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.plotter.latency.properties;

import org.HdrHistogram.AbstractHistogram;
import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class DefaultHistogramHandler implements HistogramHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultHistogramHandler.class);
    private final double unitRatio;

    public DefaultHistogramHandler() {
        this(1.0);
    }

    public DefaultHistogramHandler(double unitRatio) {
        this.unitRatio = unitRatio;
    }


    private void doSave(final EncodableHistogram eh, final File histogramFile) throws IOException {
        Properties prop = new Properties();

        prop.setProperty("latencyStartTS", Long.toString(eh.getStartTimeStamp()));
        prop.setProperty("latencyEndTS", Long.toString(eh.getEndTimeStamp()));

        prop.setProperty("latencyMaxValue", Double.toString(eh.getMaxValueAsDouble() / unitRatio));


        if (eh instanceof AbstractHistogram) {
            AbstractHistogram ah = (AbstractHistogram) eh;

            prop.setProperty("latency50th", Long.toString(ah.getValueAtPercentile(50.0) / (long) unitRatio));
            prop.setProperty("latency90th", Long.toString(ah.getValueAtPercentile(90.0) / (long) unitRatio));
            prop.setProperty("latency95th", Long.toString(ah.getValueAtPercentile(95.0) / (long) unitRatio));
            prop.setProperty("latency99th", Long.toString(ah.getValueAtPercentile(99.0) / (long) unitRatio));
            prop.setProperty("latency999th", Long.toString(ah.getValueAtPercentile(99.9) / (long) unitRatio));
            prop.setProperty("latency9999th", Long.toString(ah.getValueAtPercentile(99.99) / (long) unitRatio));
            prop.setProperty("latency99999th", Long.toString(ah.getValueAtPercentile(99.999) / (long) unitRatio));
            prop.setProperty("latencyStdDeviation", Double.toString(ah.getStdDeviation() / unitRatio));
            prop.setProperty("latencyTotalCount", Long.toString(ah.getTotalCount()));
            prop.setProperty("latencyMean", Double.toString(ah.getMean() / unitRatio));


        }
        else {
            if (eh instanceof DoubleHistogram) {
                DoubleHistogram dh = (DoubleHistogram) eh;

                prop.setProperty("latency50th", Double.toString(dh.getValueAtPercentile(50.0) / unitRatio));
                prop.setProperty("latency90th", Double.toString(dh.getValueAtPercentile(90.0) / unitRatio));
                prop.setProperty("latency95th", Double.toString(dh.getValueAtPercentile(95.0) / unitRatio));
                prop.setProperty("latency99th", Double.toString(dh.getValueAtPercentile(99.0) / unitRatio));
                prop.setProperty("latency999th", Double.toString(dh.getValueAtPercentile(99.9) / unitRatio));
                prop.setProperty("latency9999th", Double.toString(dh.getValueAtPercentile(99.99) / unitRatio));
                prop.setProperty("latency99999th", Double.toString(dh.getValueAtPercentile(99.999) / unitRatio));
                prop.setProperty("latencyStdDeviation", Double.toString(dh.getStdDeviation() / unitRatio));
                prop.setProperty("latencyTotalCount", Long.toString(dh.getTotalCount()));
                prop.setProperty("latencyMean", Double.toString(dh.getMean() / unitRatio));
            }
        }

        File outFile = new File(histogramFile.getParentFile(), "latency.properties");
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(outFile))) {
            prop.store(fos, "hdr-histogram-plotter");
        }
    }

    @Override
    public void handle(final Histogram accumulatedHistogram, final File histogramFile) throws Exception {
        logger.trace("Writing properties to {}/latency.properties", histogramFile.getPath());

        doSave(accumulatedHistogram, histogramFile);
    }
}
