package org.maestro.plotter.amqp.inspector.memory;


import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.maestro.plotter.amqp.inspector.Utilities;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkData;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkRecord;
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
@PropertyName(name="qdmemory-")
public class QDMemoryDataSet {
    private static final Logger logger = LoggerFactory.getLogger(QDMemoryDataSet.class);
    private Map<String, QDMemoryData> map = new HashMap<>();

    private static final String TOTALALLOCFROMHEAP = "totalallocfromheap";
    private static final String TYPESIZE = "typesize";
    private static final String HELDBYTHREADS = "heldbythreads";

    /**
     * Add a record to the data set
     * @param qdMemoryRecord record
     */
    public void add(final QDMemoryRecord qdMemoryRecord) {
        QDMemoryData qdMemoryData = map.get(qdMemoryRecord.getName());

        if (qdMemoryData == null) {
            qdMemoryData = new QDMemoryData();
        }

        qdMemoryData.add(qdMemoryRecord);
        map.put(qdMemoryRecord.getName(), qdMemoryData);
    }

    /**
     * Get all records
     * @return map of records
     */
    @PropertyProvider(name="")
    public Map<String, QDMemoryData> getMap() {
        return map;
    }


    private void doCalc(Map<String, Map<Instant, SummaryStatistics>> calcMap, final String key, final QDMemoryData data) {
        logger.trace("Processing record at instant for queue {}", key);

        Set<QDMemoryRecord> qdMemoryRecords = data.getRecordSet();

        for (QDMemoryRecord record : qdMemoryRecords) {
            Instant instant = record.getTimestamp();

            Utilities.putStatisticsRecord(calcMap, record.getTotalAllocFromHeap(), TOTALALLOCFROMHEAP, instant);
            Utilities.putStatisticsRecord(calcMap, record.getTypeSize(), TYPESIZE, instant);
            Utilities.putStatisticsRecord(calcMap, record.getHeldByThreads(), HELDBYTHREADS, instant);
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
