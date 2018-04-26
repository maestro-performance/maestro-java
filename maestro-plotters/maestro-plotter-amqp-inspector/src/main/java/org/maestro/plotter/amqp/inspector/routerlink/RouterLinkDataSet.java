package org.maestro.plotter.amqp.inspector.routerlink;


import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
    private Map<String, RouterLinkData> map = new HashMap<>();

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


    private void doCalc(Map<Instant, SummaryStatistics> calcMap, final String key, final RouterLinkData data) {
        logger.trace("Processing record at instant for queue {}", key);

        Set<RouterLinkRecord> routerLinkRecords = data.getRecordSet();

        for (RouterLinkRecord record : routerLinkRecords) {
            SummaryStatistics summaryStatistics = calcMap.get(record.getTimestamp());
            if (summaryStatistics == null) {
                summaryStatistics = new SummaryStatistics();
            }

            summaryStatistics.addValue(record.getUnsettledCount());
            calcMap.put(record.getTimestamp(), summaryStatistics);
        }
    }

    /**
     * Gets the aggregated statistics for the set on a per period basis. Do not confuse
     * with the statistics returned on a per-queue basis.
     * @return statistics
     */
    public Map<Date, Statistics> getStatistics() {
        Map<Instant, SummaryStatistics> calcMap = new HashMap<>();
        map.forEach((key, value) -> doCalc(calcMap, key, value));

        Map<Date, Statistics> ret = new TreeMap<>();
        calcMap.forEach((key, value) -> ret.put(Date.from(key), new Statistics(value)));

        return ret;
    }
}
