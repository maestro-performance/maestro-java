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

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.maestro.plotter.common.statistics.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * The data set for the multiple queues. By default, won't save properties for this since it generates
 * far too much data in a format that is hard to parse (due to the different queue names possible).
 */
@PropertyName(name="queue-")
public class QueueDataSet {
    private static final Logger logger = LoggerFactory.getLogger(QueueDataSet.class);
    private final Map<String, QueueData> map = new HashMap<>();

    /**
     * Add a record to the data set
     * @param queuesRecord the queues record to add
     */
    public void add(final QueuesRecord queuesRecord) {
        QueueData queueData = map.get(queuesRecord.getName());

        if (queueData == null) {
            queueData = new QueueData();
        }

        queueData.add(queuesRecord);
        map.put(queuesRecord.getName(), queueData);
    }


    /**
     * Get all records
     * @return all the records in a map
     */
    @PropertyProvider(name="")
    public Map<String, QueueData> getMap() {
        return map;
    }


    private void doCalc(Map<Instant, SummaryStatistics> calcMap, final String key, final QueueData data) {
        logger.trace("Processing record at instant for queue {}", key);

        Set<QueuesRecord> queuesRecords = data.getRecordSet();

        for (QueuesRecord record : queuesRecords) {
            SummaryStatistics summaryStatistics = calcMap.get(record.getTimestamp());
            if (summaryStatistics == null) {
                summaryStatistics = new SummaryStatistics();
            }

            summaryStatistics.addValue(record.getCount());
            calcMap.put(record.getTimestamp(), summaryStatistics);
        }
    }

    /**
     * Gets the aggregated statistics for the set on a per period basis. Do not confuse
     * with the statistics returned on a per-queue basis.
     * @return the statistics in a map
     */
    public Map<Date, Statistics> getStatistics() {
        Map<Instant, SummaryStatistics> calcMap = new HashMap<>();
        map.forEach((key, value) -> doCalc(calcMap, key, value));

        Map<Date, Statistics> ret = new TreeMap<>();
        calcMap.forEach((key, value) -> ret.put(Date.from(key), new Statistics(value)));

        return ret;
    }
}
