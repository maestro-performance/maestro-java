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

package org.maestro.plotter.inspector.serializer;

import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasData;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasDataSet;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasReader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * Serializer for JVM memory areas information
 */
public class MemoryAreasDataSerializer implements MaestroSerializer<List<MemoryData>> {
    private static final String dataName = "jvm-memory-areas";
    private MemoryAreasReader reader = new MemoryAreasReader();

    private MemoryData transform(final String name, final MemoryAreasData mad) {
        MemoryData ret = new MemoryData();

        ret.setCommitted(mad.getCommitted());
        ret.setUsed(mad.getUsed());
        ret.setPeriods(mad.getPeriods());
        ret.setName(name);

        return ret;
    }

    @Override
    public List<MemoryData> serialize(File file) throws IOException {
        MemoryAreasDataSet ds = reader.read(file);
        List<MemoryData> memoryDataList = new LinkedList<>();

        ds.getMap().forEach((k, v) -> memoryDataList.add(transform(k, v)));

        return memoryDataList;
    }

    @Override
    public String name() {
        return dataName;
    }
}
