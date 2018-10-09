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
import org.maestro.plotter.latency.DefaultHdrLogProcessorWrapper;
import org.maestro.plotter.latency.HdrLogProcessorWrapper;
import org.maestro.plotter.latency.common.HdrData;


public class DefaultLatencySerializer extends LatencySerializer {
    private double unitRate = 1.0;

    protected HdrData getHdrDataUnbounded(final Histogram histogram) {
        final HdrLogProcessorWrapper processorWrapper = new DefaultHdrLogProcessorWrapper(unitRate);

        return processorWrapper.convertLog(histogram);
    }


    protected HdrData getHdrDataBounded(final Histogram histogram, final long interval) {
        final HdrLogProcessorWrapper processorWrapper = new DefaultHdrLogProcessorWrapper(unitRate);

        return processorWrapper.convertLog(histogram, interval);
    }
}
