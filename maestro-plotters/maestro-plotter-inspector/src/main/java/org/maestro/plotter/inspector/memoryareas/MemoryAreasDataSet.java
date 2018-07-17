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

import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The data set for the multiple possible JVM memory areas
 */
@PropertyName(name="jvmMemory")
public class MemoryAreasDataSet {
    public static final String DEFAULT_FILENAME = "memory-areas.properties";

    private final Map<String, MemoryAreasData> map = new HashMap<>();

    /**
     * Add a record to the data set
     * @param memoryAreasRecord memory areas record
     */
    public void add(final MemoryAreasRecord memoryAreasRecord) {
        MemoryAreasData memoryAreasData = map.get(memoryAreasRecord.getName());

        if (memoryAreasData == null) {
            memoryAreasData = new MemoryAreasData();
        }

        memoryAreasData.add(memoryAreasRecord);
        map.put(memoryAreasRecord.getName(), memoryAreasData);
    }


    /**
     * Get all records
     * @return a map containing all the records in this set
     */
    @PropertyProvider(name="")
    public Map<String, MemoryAreasData> getMap() {
        return map;
    }
}
