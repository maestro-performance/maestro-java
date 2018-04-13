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

package org.maestro.plotter.inspector.common;

import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.common.statistics.StatisticsBuilder;

import java.util.*;

/**
 * Heap data container
 */
public class CommonMemoryData<T extends CommonMemoryRecord> implements ReportData {
    private Set<T> recordSet = new TreeSet<>();
    private Statistics usedStatistics = null;
    private Statistics committedStatistics = null;


    public void add(T record) {
        recordSet.add(record);
    }

    public List<Date> getPeriods() {
        List<Date> list = new ArrayList<>(recordSet.size());

        recordSet.stream().forEach(item->list.add(Date.from(item.getTimestamp())));

        return list;
    }

    public int getNumberOfSamples() {
        return recordSet.size();
    }

    /**
     * Get the statistics for the used heap
     * @return A Statistics object for the heap usage
     */
    public Statistics usedStatistics() {
        if (usedStatistics == null) {
            usedStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(CommonMemoryRecord::getUsed));
        }

        return usedStatistics;
    }


    /**
     * Get the statistics for the committed heap
     * @return A Statistics object for the committed usage
     */
    public Statistics committedStatistics() {
        if (committedStatistics == null) {
            committedStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(CommonMemoryRecord::getCommitted));
        }

        return committedStatistics;
    }
}
