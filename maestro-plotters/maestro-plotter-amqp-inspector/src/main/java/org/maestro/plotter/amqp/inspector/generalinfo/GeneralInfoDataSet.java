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


    private void doCalc(Map<Instant, SummaryStatistics> calcMap, final String key, final GeneralInfoData data) {
        logger.trace("Processing record at instant for queue {}", key);

        Set<GeneralInfoRecord> routerLinkRecords = data.getRecordSet();

        for (GeneralInfoRecord record : routerLinkRecords) {
            SummaryStatistics summaryStatistics = calcMap.get(record.getTimestamp());
            if (summaryStatistics == null) {
                summaryStatistics = new SummaryStatistics();
            }

            // TODO: 4/27/18 Change or extend this for multiple statistics to save
            summaryStatistics.addValue(record.getAddressCount());
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
