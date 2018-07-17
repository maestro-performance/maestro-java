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
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.common.statistics.StatisticsBuilder;

import java.util.*;

/**
 * Heap data container
 */
public class CommonMemoryData<T extends CommonMemoryRecord> implements ReportData {
    private final Set<T> recordSet = new TreeSet<>();
    private Statistics usedStatistics = null;
    private Statistics committedStatistics = null;


    public void add(T record) {
        recordSet.add(record);
    }

    public List<Date> getPeriods() {
        List<Date> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(Date.from(item.getTimestamp())));

        return list;
    }

    @Override
    public Set<T> getRecordSet() {
        return new TreeSet<>(recordSet);
    }

    /**
     * Get the used memory records
     * @param scale the scale to use (ie.: to convert from bytes to kilobytes). Default = 1
     * @return the used memory records
     */
    public List<Long> getUsed(long scale) {
        List<Long> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(item.getUsed() / scale));

        return list;
    }


    /**
     * Get the used memory records
     * @return the used memory records
     */
    public List<Long> getUsed() {
        return getUsed(1);
    }



    /**
     * Get the committed memory records
     * @param scale the scale to use (ie.: to convert from bytes to kilobytes). Default = 1
     * @return the committed memory records
     */
    public List<Long> getCommitted(long scale) {
        List<Long> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(item.getCommitted() / scale));

        return list;
    }


    /**
     * Get the committed memory records
     * @return the committed memory records
     */
    public List<Long> getCommitted() {
        return getCommitted(1);
    }


    /**
     * Get the initial memory records
     * @param scale the scale to use (ie.: to convert from bytes to kilobytes). Default = 1
     * @return the initial memory records
     */
    public List<Long> getInitial(long scale) {
        List<Long> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(item.getInitial() / scale));

        return list;
    }


    /**
     * Get the initial memory records
     * @return the initial memory records
     */
    public List<Long> getInitial() {
        return getInitial(1);
    }

    /**
     * Get the max memory records
     * @param scale the scale to use (ie.: to convert from bytes to kilobytes). Default = 1
     * @return the max memory records
     */
    public List<Long> getMax(long scale) {
        List<Long> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(item.getMax() / scale));

        return list;
    }


    /**
     * Get the max memory records
     * @return the max memory records
     */
    public List<Long> getMax() {
        return getMax(1);
    }

    public int getNumberOfSamples() {
        return recordSet.size();
    }

    /**
     * Get the statistics for the used heap
     * @return A Statistics object for the heap usage
     */
    @PropertyProvider(name="used")
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
    @PropertyProvider(name="committed")
    public Statistics committedStatistics() {
        if (committedStatistics == null) {
            committedStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(CommonMemoryRecord::getCommitted));
        }

        return committedStatistics;
    }
}
