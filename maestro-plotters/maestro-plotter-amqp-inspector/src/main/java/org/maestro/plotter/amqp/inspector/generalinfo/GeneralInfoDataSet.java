package org.maestro.plotter.amqp.inspector.generalinfo;


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
public class GeneralInfoDataSet {
    private static final Logger logger = LoggerFactory.getLogger(GeneralInfoDataSet.class);
    private Map<String, GeneralInfoData> map = new HashMap<>();

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
            Map <Instant, SummaryStatistics> map = calcMap.get(ADDRESSCOUNT);
            if (map == null){
                map = new HashMap<>();
            }
            SummaryStatistics summaryStatistics = map.get(record.getTimestamp());
            if (summaryStatistics == null) {
                summaryStatistics = new SummaryStatistics();
            }

            summaryStatistics.addValue(record.getAddressCount());
            map.put(record.getTimestamp(), summaryStatistics);
            calcMap.put(ADDRESSCOUNT, map);

            map = calcMap.get(CONNECTIONSCOUNT);
            if (map == null){
                map = new HashMap<>();
            }
            summaryStatistics = map.get(record.getTimestamp());
            if (summaryStatistics == null) {
                summaryStatistics = new SummaryStatistics();
            }

            summaryStatistics.addValue(record.getConnetionsCount());
            map.put(record.getTimestamp(), summaryStatistics);
            calcMap.put(CONNECTIONSCOUNT, map);

            map = calcMap.get(LINKROUTESSTATISTICS);
            if (map == null){
                map = new HashMap<>();
            }
            summaryStatistics = map.get(record.getTimestamp());
            if (summaryStatistics == null) {
                summaryStatistics = new SummaryStatistics();
            }

            summaryStatistics.addValue(record.getLinkRoutersCount());
            map.put(record.getTimestamp(), summaryStatistics);
            calcMap.put(LINKROUTESSTATISTICS, map);

            map = calcMap.get(AUTOLINKSTATISTICS);
            if (map == null){
                map = new HashMap<>();
            }
            summaryStatistics = map.get(record.getTimestamp());
            if (summaryStatistics == null) {
                summaryStatistics = new SummaryStatistics();
            }

            summaryStatistics.addValue(record.getAutoLinksCount());
            map.put(record.getTimestamp(), summaryStatistics);
            calcMap.put(AUTOLINKSTATISTICS, map);
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
        calcMap.forEach((key, value) -> ret.put(key, reCastMap(value)));

        return ret;
    }

    /**
     * Recast map for better evaluation by plotter.
     * @param map map with timestamp and summary statistics
     * @return map with date and statistics
     */
    private Map<Date, Statistics> reCastMap(Map<Instant, SummaryStatistics> map) {
        Map<Date, Statistics> castedmap = new HashMap<>();
        for (Map.Entry<Instant, SummaryStatistics> entry : map.entrySet())
        {
            castedmap.put(Date.from(entry.getKey()), new Statistics(entry.getValue()));
        }
        return castedmap;
    }
}
