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

package org.maestro.plotter.amqp.inspector.graph;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.amqp.inspector.memory.QDMemoryDataSet;
import org.maestro.plotter.common.graph.AbstractInterconnectPlotter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * A plotter for memory data
 */
public class QDMemoryPlotter extends AbstractInterconnectPlotter<QDMemoryDataSet> {
    public static final String DEFAULT_FILENAME = "qdmemory_";

    /**
     * Plotter
     * @param dataSet collected data
     * @param outputDir output file
     * @throws MaestroException implementation specific
     */
    @Override
    public void plot(final QDMemoryDataSet dataSet, final File outputDir) throws MaestroException {
        final Map<String, Map<Date, Statistics>> stats = dataSet.getStatistics();

        for (Map.Entry<String, Map<Date, Statistics>> entry : stats.entrySet())
        {
            plot(entry.getValue(), outputDir, entry.getKey());
        }
    }

    @Override
    public String getDefaultName() {
        return DEFAULT_FILENAME;
    }
}
