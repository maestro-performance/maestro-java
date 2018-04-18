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

package org.maestro.plotter.inspector.heap;

import org.maestro.plotter.common.RecordProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HeapProcessor implements RecordProcessor {
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final HeapData heapData = new HeapData();

    public HeapProcessor() {

    }


    public HeapData getHeapData() {
        return heapData;
    }

    //// Timestamp,Name,Initial,Max,Committed,Used
    @Override
    public void process(String... records) throws Exception {
        Date timeStamp = formatter.parse(records[0]);
        HeapRecord heapRecord = new HeapRecord();
        heapRecord.setTimestamp(timeStamp.toInstant());

        heapRecord.setName(records[1]);
        heapRecord.setInitial(Long.parseLong(records[2]));
        heapRecord.setMax(Long.parseLong(records[3]));
        heapRecord.setCommitted(Long.parseLong(records[4]));
        heapRecord.setUsed(Long.parseLong(records[5]));

        heapData.add(heapRecord);
    }
}
