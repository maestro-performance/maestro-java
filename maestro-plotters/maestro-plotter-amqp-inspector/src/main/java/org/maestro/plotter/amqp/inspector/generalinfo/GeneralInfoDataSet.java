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

package org.maestro.plotter.amqp.inspector.generalinfo;


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
public class GeneralInfoDataSet {
    private static final Logger logger = LoggerFactory.getLogger(GeneralInfoDataSet.class);
    private final Map<String, GeneralInfoData> map = new HashMap<>();

    private static final String ADDRESSCOUNT = "addressCount";
    private static final String CONNECTIONSCOUNT = "connectionsCount";
    private static final String LINKROUTESSTATISTICS = "linkRoutesStatistics";
    private static final String AUTOLINKSTATISTICS = "autoLinksStatistics";

    /**
     * Add a record to the data set
     * @param generalInfoRecord record
     */
    public void add(final GeneralInfoRecord generalInfoRecord) {
        GeneralInfoData generalInfoData = map.get(generalInfoRecord.getName());

        if (generalInfoData == null) {
            generalInfoData = new GeneralInfoData();
        }

        generalInfoData.add(generalInfoRecord);
        map.put(generalInfoRecord.getName(), generalInfoData);
    }


    /**
     * Get all records
     * @return map of records
     */
    @PropertyProvider(name="")
    public Map<String, GeneralInfoData> getMap() {
        return map;
    }


    private void doCalc(Map<String, Map<Instant, SummaryStatistics>> calcMap, final String key, final GeneralInfoData data) {
        logger.trace("Processing record at instant for queue {}", key);

        Set<GeneralInfoRecord> generalInfoRecords = data.getRecordSet();

        for (GeneralInfoRecord record : generalInfoRecords) {
            Instant instant = record.getTimestamp();

            Utilities.putStatisticsRecord(calcMap, record.getAddressCount(), ADDRESSCOUNT, instant);
            Utilities.putStatisticsRecord(calcMap, record.getConnetionsCount(), CONNECTIONSCOUNT, instant);
            Utilities.putStatisticsRecord(calcMap, record.getLinkRoutersCount(), LINKROUTESSTATISTICS, instant);
            Utilities.putStatisticsRecord(calcMap, record.getAutoLinksCount(), AUTOLINKSTATISTICS, instant);
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
