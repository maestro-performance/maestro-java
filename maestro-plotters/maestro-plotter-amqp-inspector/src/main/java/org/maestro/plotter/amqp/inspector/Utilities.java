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

package org.maestro.plotter.amqp.inspector;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.maestro.plotter.common.statistics.Statistics;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utilities{
    private Utilities() {
    }

    private static SummaryStatistics getSummaryStatistics(Map<Instant, SummaryStatistics> map, Instant instant) {
        SummaryStatistics summaryStatistics = map.get(instant);

        return summaryStatistics == null ? new SummaryStatistics() : summaryStatistics;
    }

    private static Map<Instant, SummaryStatistics> getMap(Map<String, Map<Instant, SummaryStatistics>> map, String key){
        Map<Instant, SummaryStatistics> returnMap = map.get(key);

        return returnMap == null ? new HashMap<>() : returnMap;
    }

    public static void putStatisticsRecord(
            Map<String, Map<Instant, SummaryStatistics>> calcMap,
            Long data, String key,
            Instant instant) {

        Map <Instant, SummaryStatistics> map = getMap(calcMap, key);
        SummaryStatistics summaryStatistics = getSummaryStatistics(map, instant);
        summaryStatistics.addValue(data);
        map.put(instant, summaryStatistics);

        calcMap.put(key, map);
    }

    /**
     * Recast map for better evaluation by plotter.
     * @param map map with timestamp and summary statistics
     * @return map with date and statistics
     */
    public static Map<Date, Statistics> reCastMap(Map<Instant, SummaryStatistics> map) {
        Map<Date, Statistics> castedmap = new HashMap<>();
        for (Map.Entry<Instant, SummaryStatistics> entry : map.entrySet())
        {
            castedmap.put(Date.from(entry.getKey()), new Statistics(entry.getValue()));
        }
        return castedmap;
    }
}
