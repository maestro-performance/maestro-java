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

package org.maestro.reports.plotter;

import org.maestro.plotter.common.BasicPlotter;
import org.maestro.plotter.graph.HeapPlotter;
import org.maestro.plotter.graph.MemoryAreasPlotter;
import org.maestro.plotter.graph.QueuePlotter;
import org.maestro.plotter.inspector.heap.HeapReader;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasReader;
import org.maestro.plotter.inspector.queues.QueueReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class InspectorPlotterWrapper implements PlotterWrapper {
    private static final Logger logger = LoggerFactory.getLogger(InspectorPlotterWrapper.class);

    private boolean plotHeap(final File file) {
        BasicPlotter<HeapReader, HeapPlotter> basicPlotter = new BasicPlotter<>(new HeapReader(), new HeapPlotter());

        File outputFile = new File(file.getParentFile(), HeapPlotter.DEFAULT_FILENAME);
        try {
            basicPlotter.plot(file, outputFile);
        } catch (Exception e) {
            logger.error("Unable to plot file {}: {}", file.getPath(), e.getMessage(), e);
            return false;
        }
        return true;
    }

    private boolean plotMemoryAreas(final File file) {
        BasicPlotter<MemoryAreasReader, MemoryAreasPlotter> basicPlotter = new BasicPlotter<>(new MemoryAreasReader(),
                new MemoryAreasPlotter());

        try {
            basicPlotter.plot(file, file.getParentFile());
        } catch (Exception e) {
            logger.error("Unable to plot file {}: {}", file.getPath(), e.getMessage(), e);
            return false;
        }

        return true;
    }

    private boolean plotQueues(final File file) {
        BasicPlotter<QueueReader, QueuePlotter> basicPlotter = new BasicPlotter<>(new QueueReader(),
                new QueuePlotter());

        try {
            basicPlotter.plot(file, file.getParentFile());
        } catch (Exception e) {
            logger.error("Unable to plot file {}: {}", file.getPath(), e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public boolean plot(final File file) {
        boolean ret = false;

        switch (file.getName()) {
            case "heap.csv":
                ret = plotHeap(file);
                break;
            case "memory-areas.csv":
                ret = plotMemoryAreas(file);
                break;
            case "queues.csv":
                ret = plotQueues(file);
                break;
        }

        return ret;
    }
}
