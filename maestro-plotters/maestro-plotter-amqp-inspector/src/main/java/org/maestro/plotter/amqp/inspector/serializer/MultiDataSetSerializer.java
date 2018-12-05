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

package org.maestro.plotter.amqp.inspector.serializer;

import org.maestro.plotter.amqp.inspector.common.MultiDataSet;
import org.maestro.plotter.common.readers.StreamReader;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;


/**
 * Serializer for queue statistics
 */
public class MultiDataSetSerializer<Y extends MultiDataSet, T extends StreamReader<Y>> implements MaestroSerializer<Map<String, Map<Date, Statistics>>> {
    private final String dataName;

    private final T reader;

    protected MultiDataSetSerializer(final T reader, final String dataName) {
        this.reader = reader;
        this.dataName = dataName;
    }

    @Override
    public Map<String, Map<Date, Statistics>> serialize(File file) throws IOException {
        Y dataSet = reader.read(file);

        return dataSet.getStatistics();
    }

    @Override
    public String name() {
        return dataName;
    }
}
