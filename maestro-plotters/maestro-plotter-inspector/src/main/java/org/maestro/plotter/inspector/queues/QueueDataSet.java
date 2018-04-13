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

import java.util.HashMap;
import java.util.Map;

/**
 * The data set for the multiple queues
 */
public class QueueDataSet {
    private Map<String, QueueData> map = new HashMap<>();

    /**
     * Add a record to the data set
     * @param queuesRecord
     */
    public void add(final QueuesRecord queuesRecord) {
        QueueData queueData = map.get(queuesRecord.getName());

        if (queueData == null) {
            queueData = new QueueData();
        }

        queueData.add(queuesRecord);
        map.put(queuesRecord.getName(), queueData);
    }


    /**
     * Get all records
     * @return
     */
    public Map<String, QueueData> getMap() {
        return map;
    }
}
