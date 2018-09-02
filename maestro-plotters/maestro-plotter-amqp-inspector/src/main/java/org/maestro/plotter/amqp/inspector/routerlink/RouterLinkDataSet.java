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

package org.maestro.plotter.amqp.inspector.routerlink;


import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.maestro.plotter.amqp.inspector.Utilities;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.maestro.plotter.common.statistics.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * A class represents router link data set
 */
@PropertyName(name="routerLink-")
public class RouterLinkDataSet {
    private static final Logger logger = LoggerFactory.getLogger(RouterLinkDataSet.class);
    private final Map<String, RouterLinkData> map = new HashMap<>();

    private static final String DELIVERED = "delivered";
    private static final String UNSETTLED = "unsettled";
    private static final String UNDELIVERED = "undelivered";
    private static final String ACCEPTED = "accepted";
    private static final String RELEASED = "released";

    /**
     * Add a record to the data set
     * @param routerLinkRecord record
     */
    public void add(final RouterLinkRecord routerLinkRecord) {
        RouterLinkData routerLinkData = map.get(routerLinkRecord.getName());

        if (routerLinkData == null) {
            routerLinkData = new RouterLinkData();
        }

        routerLinkData.add(routerLinkRecord);
        map.put(routerLinkRecord.getName(), routerLinkData);
    }

    /**
     * Get all records
     * @return map of records
     */
    @PropertyProvider(name="")
    public Map<String, RouterLinkData> getMap() {
        return map;
    }


    private void doCalc(Map<String, Map<Instant, SummaryStatistics>> calcMap, final String key, final RouterLinkData data) {
        logger.trace("Processing record at instant for queue {}", key);

        Set<RouterLinkRecord> routerLinkRecords = data.getRecordSet();

        for (RouterLinkRecord record : routerLinkRecords) {
            Instant instant = record.getTimestamp();

            Utilities.putStatisticsRecord(calcMap, record.getDeliveryCount(), DELIVERED, instant);
            Utilities.putStatisticsRecord(calcMap, record.getUndeliveredCount(), UNDELIVERED, instant);
            Utilities.putStatisticsRecord(calcMap, record.getUnsettledCount(), UNSETTLED, instant);
            Utilities.putStatisticsRecord(calcMap, record.getAcceptedCount(), ACCEPTED, instant);
            Utilities.putStatisticsRecord(calcMap, record.getReleasedCount(), RELEASED, instant);
        }
    }

    /**
     * Gets the aggregated statistics for the set on a per period basis. Do not confuse
     * with the statistics returned on a per-queue basis.
     * @return statistics
     */
    public Map<String, Map<Date, Statistics>> getStatistics() {
        Map<String, Map<Instant, SummaryStatistics>> calcMap = new HashMap<>();
        map.forEach((key, value) -> doCalc(calcMap, key, value));

        Map<String, Map<Date, Statistics>> ret = new TreeMap<>();
        calcMap.forEach((key, value) -> ret.put(key, Utilities.reCastMap(value)));

        return ret;
    }
}
