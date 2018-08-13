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

import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.amqp.inspector.generalinfo.GeneralInfoReader;
import org.maestro.plotter.amqp.inspector.graph.GeneralInfoPlotter;
import org.maestro.plotter.amqp.inspector.graph.QDMemoryPlotter;
import org.maestro.plotter.amqp.inspector.graph.RouterLinkPlotter;
import org.maestro.plotter.amqp.inspector.memory.QDMemoryReader;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkReader;
import org.maestro.plotter.common.BasicPlotter;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.inspector.graph.HeapPlotter;
import org.maestro.plotter.inspector.graph.MemoryAreasPlotter;
import org.maestro.plotter.inspector.graph.QueuePlotter;
import org.maestro.plotter.inspector.heap.HeapReader;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasDataSet;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasReader;
import org.maestro.plotter.inspector.queues.QueueReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class InspectorPlotterWrapper implements PlotterWrapper {
    private static final Logger logger = LoggerFactory.getLogger(InspectorPlotterWrapper.class);

    private boolean plotHeap(final File file) {
        BasicPlotter<HeapReader, HeapPlotter> basicPlotter = new BasicPlotter<>(new HeapReader(), new HeapPlotter());

        File outputFile = new File(file.getParentFile(), HeapPlotter.DEFAULT_FILENAME);
        File propertiesFile = new File(file.getParentFile(), "heap.properties");
        try {
            basicPlotter.plot(file, outputFile, propertiesFile);
        } catch (Exception e) {
            handlePlotException(file, e);
            throw e;
        }
        return true;
    }

    private boolean plotMemoryAreas(final File file) {
        try {
            MemoryAreasReader reader = new MemoryAreasReader();
            MemoryAreasDataSet dataSet = reader.read(file);

            MemoryAreasPlotter plotter = new MemoryAreasPlotter();
            plotter.plot(dataSet, file.getParentFile());

            File propertiesFile = new File(file.getParentFile(), "memory.properties");
            PropertyWriter propertyWriter = new PropertyWriter();
            propertyWriter.write(dataSet, propertiesFile);
        } catch (IOException e) {
            handlePlotException(file, e);
            throw new MaestroException(e);
        } catch (Exception e) {
            handlePlotException(file, e);
            throw e;
        }

        return true;
    }

    private boolean plotQueues(final File file) {
        BasicPlotter<QueueReader, QueuePlotter> basicPlotter = new BasicPlotter<>(new QueueReader(),
                new QueuePlotter());

        try {
            File outputFile = new File(file.getParentFile(), "queues.png");

            basicPlotter.plot(file, outputFile, null);
        } catch (Exception e) {
            handlePlotException(file, e);
            throw e;
        }

        return true;
    }

    private boolean plotGeneral(final File file) {
        BasicPlotter<GeneralInfoReader, GeneralInfoPlotter> basicPlotter = new BasicPlotter<>(new GeneralInfoReader(),
                new GeneralInfoPlotter());

        try {
            File outputFile = file.getParentFile();

            basicPlotter.plot(file, outputFile, null);
        } catch (Exception e) {
            handlePlotException(file, e);
            throw e;
        }

        return true;
    }

    private boolean plotQDMemory(final File file) {
        BasicPlotter<QDMemoryReader, QDMemoryPlotter> basicPlotter = new BasicPlotter<>(new QDMemoryReader(),
                new QDMemoryPlotter());

        try {
            File outputFile = file.getParentFile();

            basicPlotter.plot(file, outputFile, null);
        } catch (Exception e) {
            handlePlotException(file, e);
            throw e;
        }

        return true;
    }

    private boolean plotRouterLink(final File file) {
        BasicPlotter<RouterLinkReader, RouterLinkPlotter> basicPlotter = new BasicPlotter<>(new RouterLinkReader(),
                new RouterLinkPlotter());

        try {
            File outputFile = file.getParentFile();

            basicPlotter.plot(file, outputFile, null);
        } catch (Exception e) {
            handlePlotException(file, e);
            throw e;
        }

        return true;
    }

    @Override
    public boolean plot(final File file) {
        logger.debug("Plotting Maestro Inspector report file {}", file.getPath());
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
            case "general.csv":
                ret = plotGeneral(file);
                break;
            case "qdmemory.csv":
                ret = plotQDMemory(file);
                break;
            case "routerLink.csv":
                ret = plotRouterLink(file);
                break;
        }

        return ret;
    }
}
