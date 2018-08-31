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

package org.maestro.plotter.inspector.graph;

import org.junit.Test;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasDataSet;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasProcessor;
import org.maestro.plotter.inspector.memoryareas.MemoryAreasReader;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class MemoryAreasPlotterTest {
    @Test
    public void testPlot() throws IOException {
        String fileName = this.getClass().getResource("/data-ok/memory-areas.csv").getPath();

        MemoryAreasProcessor memoryAreaProcessor = new MemoryAreasProcessor();
        MemoryAreasReader memoryAreaReader = new MemoryAreasReader(memoryAreaProcessor);

        MemoryAreasDataSet memoryAreaDataSet = memoryAreaReader.read(fileName);

        File sourceFile = new File(fileName);
        MemoryAreasPlotter plotter = new MemoryAreasPlotter();

        plotter.plot(memoryAreaDataSet, sourceFile.getParentFile());

        for (String areaName : memoryAreaDataSet.getMap().keySet()) {
            File outputFile = new File(sourceFile.getParentFile(), MemoryAreasPlotter.friendlyName(areaName));

            assertTrue("The output file does not exist: " + outputFile.getPath(), outputFile.exists());
        }
    }

    /**
     * Results in nothing because there can be 0 or more memory areas data
     * mapped in the CSV.
     * @throws IOException for I/O related errors
     */
    @Test
    public void testPlotEmpty() throws IOException {
        String fileName = this.getClass().getResource("/empty/memory-areas.csv").getPath();

        MemoryAreasProcessor memoryAreaProcessor = new MemoryAreasProcessor();
        MemoryAreasReader memoryAreaReader = new MemoryAreasReader(memoryAreaProcessor);

        MemoryAreasDataSet memoryAreaDataSet = memoryAreaReader.read(fileName);

        File sourceFile = new File(fileName);
        MemoryAreasPlotter plotter = new MemoryAreasPlotter();

        plotter.plot(memoryAreaDataSet, sourceFile.getParentFile());

        for (String areaName : memoryAreaDataSet.getMap().keySet()) {
            File outputFile = new File(sourceFile.getParentFile(), MemoryAreasPlotter.friendlyName(areaName));

            assertTrue("The output file does not exist: " + outputFile.getPath(), outputFile.exists());
        }
    }
}
