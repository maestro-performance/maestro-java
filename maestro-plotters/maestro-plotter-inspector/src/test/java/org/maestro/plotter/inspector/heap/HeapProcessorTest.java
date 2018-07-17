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

package org.maestro.plotter.inspector.heap;

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeapProcessorTest {
    private final String fileName = this.getClass().getResource("/data-ok/heap.csv").getPath();
    private HeapData heapData;


    @Before
    public void setUp() throws Exception {
        HeapProcessor heapProcessor = new HeapProcessor();
        HeapReader heapReader = new HeapReader(heapProcessor);

        heapData = heapReader.read(fileName);
    }

    @Test
    public void testBasicFile() {

        assertEquals("The number strictOf heap records don't match", 18, heapData.getNumberOfSamples());

        Statistics usedStatistics = heapData.usedStatistics();
        assertEquals("Unexpected max value for the used heap", 1465312496.0,
                usedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the used heap", 583455472.0,
                usedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the used heap", 946790919.359984,
                usedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the used heap", 300739376.885459,
                usedStatistics.getStandardDeviation(), 0.0001);

        Statistics committedStatistics = heapData.committedStatistics();
        assertEquals("Unexpected max value for the committed heap", 1738539008.0000,
                committedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the committed heap", 1738539008.0000,
                committedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the committed heap", 1738539008.0000,
                committedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the committed heap", 0,
                committedStatistics.getStandardDeviation(), 0.0001);
    }

    @Test
    public void testProperties() throws IOException {
        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), HeapData.DEFAULT_FILENAME);

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(heapData, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
