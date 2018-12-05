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

package org.maestro.plotter.latency.serializer;

import org.HdrHistogram.Histogram;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.test.TestProperties;
import org.maestro.common.test.properties.PropertyReader;
import org.maestro.common.worker.WorkerUtils;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.latency.common.HdrData;
import org.maestro.plotter.latency.common.HdrDataCO;
import org.maestro.plotter.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

abstract public class LatencySerializer implements MaestroSerializer<LatencyDistribution> {
    private static final Logger logger = LoggerFactory.getLogger(LatencySerializer.class);
    private static final String dataName = "latency";


    private TestProperties loadProperties(final File baseDir) {
        final File propertiesFile = new File(baseDir, "test.properties");

        if (propertiesFile.exists()) {
            logger.debug("Loading test.properties file to check for bounded/unbounded rate");
            TestProperties testProperties = new TestProperties();

            try {
                PropertyReader reader = new PropertyReader();

                reader.read(propertiesFile, testProperties);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return testProperties;
        }

        return null;
    }

    private synchronized HdrData getHdrData(final Histogram histogram, final File file) {
        HdrData hdrData;

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

        return hdrData;
    }

    protected abstract HdrData getHdrDataUnbounded(final Histogram histogram);

    protected abstract HdrData getHdrDataBounded(final Histogram histogram, final long interval);

    private Latency getLatencyInfo(final Histogram histogram, final HdrData hdrData, final File file) {
        Latency latency = new Latency();

        latency.setPercentiles(hdrData.getPercentile());
        latency.setValues(hdrData.getValue());

        LatencySerializerProcessor latencySerializerProcessor = new LatencySerializerProcessor(latency);

        try {
            latencySerializerProcessor.postProcess(histogram, file);
        } catch (Exception e) {
            throw new MaestroException("Unable to run the post processing of the latency data", e);
        }

        return latency;
    }

    @Override
    public LatencyDistribution serialize(final File file) throws IOException {
        Histogram histogram = Util.getAccumulated(file);

        HdrData hdrData = getHdrData(histogram, file);

        LatencyDistribution latencyDistribution = new LatencyDistribution();

        if (hdrData instanceof HdrDataCO) {
            Latency corrected = getLatencyInfo(histogram, ((HdrDataCO) hdrData).getCorrected(), file);
            latencyDistribution.getLatencyDistribution().put("responseTime", corrected);

            Latency normal = getLatencyInfo(histogram, hdrData, file);
            latencyDistribution.getLatencyDistribution().put("serviceTime", normal);
        } else {
            Latency normal = getLatencyInfo(histogram, hdrData, file);
            latencyDistribution.getLatencyDistribution().put("serviceTime", normal);
        }

        return latencyDistribution;
    }

    @Override
    public String name() {
        return dataName;
    }
}
