package org.maestro.plotter.amqp.inspector.memory;

import org.maestro.plotter.common.RecordProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class for read data processor
 */
public class QDMemoryProcessor implements RecordProcessor {
    private final QDMemoryDataSet qdMemoryDataSet = new QDMemoryDataSet();

    public QDMemoryDataSet getQDMemoryDataSet() {
        return qdMemoryDataSet;
    }

//    Timestamp,Name,Size,Batch,Thread-max,Total,In-threads,Rebal-in,Rebal-out,totalFreeToHeap,globalFreeListMax

    /**
     * Method for process read data from csv
     * @param records records from csv
     * @throws Exception implementation specific
     */
    @Override
    public void process(String... records) throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date timeStamp = formatter.parse(records[0]);
        QDMemoryRecord qdMemoryRecord = new QDMemoryRecord();
        qdMemoryRecord.setTimestamp(timeStamp.toInstant());

        qdMemoryRecord.setName(records[1]);

        qdMemoryRecord.setTypeSize(Long.parseLong(records[2]));
        qdMemoryRecord.setTransferBatchSize(Long.parseLong(records[3]));
        qdMemoryRecord.setLocalFreeListMax(Long.parseLong(records[4]));
        qdMemoryRecord.setTotalAllocFromHeap(Long.parseLong(records[5]));
        qdMemoryRecord.setHeldByThreads(Long.parseLong(records[6]));
        qdMemoryRecord.setBatchesRebalancedToThreads(Long.parseLong(records[7]));
        qdMemoryRecord.setBatchesRebalancedToGlobal(Long.parseLong(records[8]));
        qdMemoryRecord.setTotalFreeToHeap(Long.parseLong(records[9]));
        qdMemoryRecord.setGlobalFreeListMax(Long.parseLong(records[10]));

        qdMemoryDataSet.add(qdMemoryRecord);
    }
}
