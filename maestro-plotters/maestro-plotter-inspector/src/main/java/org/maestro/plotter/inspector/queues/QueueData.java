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

package org.maestro.plotter.inspector.queues;

import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.common.statistics.StatisticsBuilder;

import java.time.Instant;
import java.util.*;


/**
 * Queue counters data
 */
@PropertyName(name="queue")
public class QueueData implements ReportData {
    public static final String DEFAULT_FILENAME = "queue.properties";

    private final Set<QueuesRecord> recordSet = new TreeSet<>();
    private Statistics countStatistics = null;
    private Statistics consumerStatistics = null;
    private Statistics addedStatistics = null;
    private Statistics expiredStatistics = null;


    public void add(QueuesRecord record) {
        recordSet.add(record);
    }

    public List<Date> getPeriods() {
        List<Date> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(Date.from(item.getTimestamp())));

        return list;
    }

    public Set<QueuesRecord> getRecordSet() {
        return new TreeSet<>(recordSet);
    }

    public QueuesRecord getAt(final Instant instant) {
        return recordSet.stream().findFirst().filter(record -> record.getTimestamp().equals(instant)).orElse(null);
    }

    /**
     * Number of records
     * @return the number of samples/records in the data set
     */
    public int getNumberOfSamples() {
        return recordSet.size();
    }

    /**
     * Get the statistics for the message count
     * @return A Statistics object for the message count
     */
    @PropertyProvider(name="-count")
    public Statistics countStatistics() {
        if (countStatistics == null) {
            countStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(QueuesRecord::getCount));
        }

        return countStatistics;
    }


    /**
     * Get the statistics for the consumer count
     * @return A Statistics object for the consumer count
     */
    @PropertyProvider(name="-consumer")
    public Statistics consumerStatistics() {
        if (consumerStatistics == null) {
            consumerStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(QueuesRecord::getConsumers));
        }

        return consumerStatistics;
    }

    /**
     * Get the statistics for the added messages
     * @return A Statistics object for the added messages
     */
    protected Statistics addedStatistics() {
        if (addedStatistics == null) {
            addedStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(QueuesRecord::getAdded));
        }

        return addedStatistics;
    }

    @PropertyProvider(name="-addedCount")
    public double getAddedCount() {
        return addedStatistics().getMax();
    }

    /**
     * Get the statistics for the expired messages
     * @return A Statistics object for the expired messages
     */
    protected Statistics expiredStatistics() {
        if (expiredStatistics == null) {
            expiredStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(QueuesRecord::getExpired));
        }

        return expiredStatistics;
    }

    @PropertyProvider(name="-expiredCount")
    public double getExpiredCount() {
        return expiredStatistics().getMax();
    }

}
