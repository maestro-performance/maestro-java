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
