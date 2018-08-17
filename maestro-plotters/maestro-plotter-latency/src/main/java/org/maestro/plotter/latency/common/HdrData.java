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

package org.maestro.plotter.latency.common;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A container for the HDR Histogram data being processed
 * Note: it does not implement ReportData because it is not the typical time series
 * data.
 */
public class HdrData {
    private final List<HdrRecord> records = new LinkedList<>();
    private final TimeUnit timeUnit;

    public HdrData() {
        this(TimeUnit.MICROSECONDS);
    }

    public HdrData(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public List<Double> getValue() {
        return records.stream().mapToDouble(record -> timeUnit.toMillis((long) record.getValue()))
                .boxed().collect(Collectors.toList());
    }

    public List<Double> getPercentile() {
        return records.stream().mapToDouble(HdrRecord::getPercentile).boxed().collect(Collectors.toList());
    }

    public void add(final HdrRecord hdrRecord) {
        records.add(hdrRecord);
    }
}
