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

package org.maestro.plotter.inspector.memoryareas;

import org.maestro.plotter.common.RecordProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The processor for JVM memory areas
 */
public class MemoryAreasProcessor implements RecordProcessor {
    private final MemoryAreasDataSet memoryAreasDataSet = new MemoryAreasDataSet();

    public MemoryAreasDataSet getMemoryAreasDataSet() {
        return memoryAreasDataSet;
    }

    //// Timestamp,Name,Initial,Max,Committed,Used
    @Override
    public void process(String... records) throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date timeStamp = formatter.parse(records[0]);
        MemoryAreasRecord memoryAreasRecord = new MemoryAreasRecord();
        memoryAreasRecord.setTimestamp(timeStamp.toInstant());

        memoryAreasRecord.setName(records[1]);
        memoryAreasRecord.setInitial(Long.parseLong(records[2]));
        memoryAreasRecord.setMax(Long.parseLong(records[3]));
        memoryAreasRecord.setCommitted(Long.parseLong(records[4]));
        memoryAreasRecord.setUsed(Long.parseLong(records[5]));

        memoryAreasDataSet.add(memoryAreasRecord);
    }
}
