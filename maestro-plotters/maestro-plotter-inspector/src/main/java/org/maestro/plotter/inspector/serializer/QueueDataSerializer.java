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

import org.apache.commons.compress.utils.Lists;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.common.serializer.SingleData;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.inspector.queues.QueueDataSet;
import org.maestro.plotter.inspector.queues.QueueReader;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Serializer for queue statistics
 */
public class QueueDataSerializer implements MaestroSerializer<SingleData<Statistics>> {
    private static final String dataName = "queues";

    private QueueReader reader = new QueueReader();

    @Override
    public SingleData<Statistics> serialize(File file) throws IOException {
        QueueDataSet dataSet = reader.read(file);

        final Map<Date, Statistics> stats = dataSet.getStatistics();
        final List<Date> periods = Lists.newArrayList(stats.keySet().iterator());
        final List<Statistics> values = Lists.newArrayList(stats.values().iterator());

        SingleData<Statistics> ret = new SingleData<>();

        ret.setPeriods(periods);
        ret.setValues(values);

        return ret;
    }

    @Override
    public String name() {
        return dataName;
    }
}
