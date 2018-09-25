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
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.latency.HdrLogProcessorWrapper;
import org.maestro.plotter.latency.common.HdrData;
import org.maestro.plotter.latency.graph.HdrPlotter;
import org.maestro.plotter.latency.properties.HdrPropertyWriter;
import org.maestro.plotter.utils.Util;

import java.io.File;
import java.io.IOException;

public class LatencySerializer implements MaestroSerializer<Latency> {
    private static final String dataName = "latency";

    @Override
    public Latency serialize(final File file) throws IOException {
        // HDR Log Reader
        HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper();

        Histogram histogram = Util.getAccumulated(file);

        HdrData hdrData = processorWrapper.convertLog(histogram);

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
    public String name() {
        return dataName;
    }
}
