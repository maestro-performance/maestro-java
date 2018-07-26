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

package org.maestro.plotter.rate;


import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.common.statistics.StatisticsBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A container for the collected rate information
 */
@PropertyName(name="rate")
public class RateData implements ReportData {
    final private Set<RateRecord> recordSet;
    private Statistics statistics;
    private long errorCount;
    private long skipCount = 0;


    public RateData() {
        recordSet = new TreeSet<>();
    }

    RateData(final Set<RateRecord> other) {
        this.recordSet = other;
    }

    public void add(RateRecord rateRecord) {
        recordSet.add(rateRecord);
    }

    @Override
    public List<Date> getPeriods() {
        final List<Date> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item -> list.add(Date.from(item.getTimestamp())));

        return list;
    }

    public List<Long> getRateValues()
    {
        return recordSet.stream().mapToLong(RateRecord::getCount).boxed().collect(Collectors.toList());
    }

    @Override
    public Set<RateRecord> getRecordSet() {
        return recordSet;
    }

    @PropertyProvider(name="samples")
    public int getNumberOfSamples() {
        return recordSet.size();
    }


    @PropertyProvider(name="")
    public Statistics rateStatistics() {
        if (statistics == null) {
            statistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(RateRecord::getCount));
        }

        return statistics;
    }


    @PropertyProvider(name="errorCount")
    public long getErrorCount() {
        return errorCount;
    }


    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    @PropertyProvider(name="skipCount")
    public long getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(long skipCount) {
        this.skipCount = skipCount;
    }
}
