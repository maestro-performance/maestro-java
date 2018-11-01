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

import org.maestro.plotter.common.ReportReader;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.inspector.common.CommonMemoryData;

import java.io.File;
import java.io.IOException;

/**
 * Base serializer for memory data
 * @param <T>
 * @param <Y>
 */
abstract class CommonMemoryDataSerializer<T extends CommonMemoryData<?>, Y extends ReportReader<T>> implements MaestroSerializer<MemoryData> {
    private final Y reader;

    CommonMemoryDataSerializer(Y reader) {
        this.reader = reader;
    }

    @Override
    public MemoryData serialize(File file) throws IOException {
        T commonMemoryData = reader.read(file);

        MemoryData ret = new MemoryData();

        ret.setCommitted(commonMemoryData.getCommitted());
        ret.setUsed(commonMemoryData.getUsed());
        ret.setName(name());

        return ret;
    }
}
