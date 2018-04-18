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

import org.maestro.plotter.common.RecordProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The processor for the recorded queue data
 */
public class QueueProcessor implements RecordProcessor {
    private final QueueDataSet queueDataSet = new QueueDataSet();

    public QueueDataSet getQueueDataSet() {
        return queueDataSet;
    }

    // Timestamp,Name,MessagesAdded,MessageCount,MessagesAcknowledged,MessagesExpired,ConsumerCount
    @Override
    public void process(String... records) throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date timeStamp = formatter.parse(records[0]);
        QueuesRecord queuesRecord = new QueuesRecord();
        queuesRecord.setTimestamp(timeStamp.toInstant());

        queuesRecord.setName(records[1]);
        queuesRecord.setAdded(Long.parseLong(records[2]));
        queuesRecord.setCount(Long.parseLong(records[3]));
        queuesRecord.setAcknowledged(Long.parseLong(records[4]));
        queuesRecord.setExpired(Long.parseLong(records[5]));
        queuesRecord.setConsumers(Long.parseLong(records[6]));

        queueDataSet.add(queuesRecord);
    }
}
