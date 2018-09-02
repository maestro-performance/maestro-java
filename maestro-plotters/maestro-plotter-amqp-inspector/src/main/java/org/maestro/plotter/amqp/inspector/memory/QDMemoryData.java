/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.plotter.amqp.inspector.memory;

import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.common.statistics.StatisticsBuilder;

import java.time.Instant;
import java.util.*;

/**
 * A class represents data about router link
 */
@PropertyName(name="qdmemory")
public class QDMemoryData implements ReportData {
    public static final String DEFAULT_FILENAME = "qdmemory.properties";

    private final Set<QDMemoryRecord> recordSet = new TreeSet<>();
    private Statistics totalAllocFromHeapStatistics = null;
    private Statistics typeSizeStatistics = null;
    private Statistics heldByThreadsStatistics = null;

    public void add(QDMemoryRecord record) {
        recordSet.add(record);
    }

    public List<Date> getPeriods() {
        List<Date> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(Date.from(item.getTimestamp())));

        return list;
    }

    public Set<QDMemoryRecord> getRecordSet() {
        return new TreeSet<>(recordSet);
    }

    public QDMemoryRecord getAt(final Instant instant) {
        return recordSet.stream().findFirst().filter(record -> record.getTimestamp().equals(instant)).orElse(null);
    }

    /**
     * Number of records
     * @return the number of records/samples
     */
    public int getNumberOfSamples() {
        return recordSet.size();
    }

    /**
     * Get the statistics for the total allocated memory from heap
     * @return A Statistics object for the total allocated memory from heap
     */
    public Statistics totalAllocFromHeapStatistics() {
        if (totalAllocFromHeapStatistics == null) {
            totalAllocFromHeapStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(QDMemoryRecord::getTotalAllocFromHeap));
        }

        return totalAllocFromHeapStatistics;
    }

    @PropertyProvider(name="-TotalAllocFromHeap")
    public double getTotalAllocFromHeap() {
        return totalAllocFromHeapStatistics().getMax();
    }

    /**
     * Get the statistics for the type size memory
     * @return A Statistics object for the type size memory
     */
    public Statistics typeSizeStatistics() {
        if (typeSizeStatistics == null) {
            typeSizeStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(QDMemoryRecord::getTypeSize));
        }

        return typeSizeStatistics;
    }

    @PropertyProvider(name="-TypeSize")
    public double getTypeSize() {
        return typeSizeStatistics().getMax();
    }

    /**
     * Get the statistics for the held by thread memory
     * @return A Statistics object for the held by thread memory
     */
    public Statistics heldByThreadsStatistics() {
        if (heldByThreadsStatistics == null) {
            heldByThreadsStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(QDMemoryRecord::getHeldByThreads));
        }

        return heldByThreadsStatistics;
    }

    @PropertyProvider(name="-HeldByThreads")
    public double getHeldByThreads() {
        return heldByThreadsStatistics().getMax();
    }
}
