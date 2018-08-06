/*
 * Copyright 2017 Otavio Rodolfo Piske
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

package org.maestro.plotter.latency;

import org.HdrHistogram.Histogram;
import org.maestro.plotter.latency.common.HdrData;
import org.maestro.plotter.latency.common.HdrDataCO;
import org.maestro.plotter.latency.common.HdrRecord;


public class HdrLogProcessorWrapper {
    private final double unitRatio;

    public HdrLogProcessorWrapper() {
        this(1.0);
    }


    public HdrLogProcessorWrapper(double unitRatio) {
        this.unitRatio = unitRatio;
    }

    private void addHdr(HdrData hdrData, double percentile, double value) {
        hdrData.add(new HdrRecord(percentile, value));
    }


    public HdrData convertLog(final Histogram histogram) {
        HdrData ret = new HdrData();

        histogram.recordedValues().forEach(value -> addHdr(ret, value.getPercentile(),
                value.getDoubleValueIteratedTo() / unitRatio));

        return ret;
    }

    public HdrDataCO convertLog(final Histogram histogram, long knownCO) {
        HdrDataCO ret = new HdrDataCO();

        histogram.recordedValues().forEach(value -> addHdr(ret, value.getPercentile(),
                value.getDoubleValueIteratedTo()));

        Histogram corrected = histogram.copyCorrectedForCoordinatedOmission(knownCO);
        HdrData correctedData = convertLog(corrected);

        ret.setCorrected(correctedData);

        return ret;
    }
}
