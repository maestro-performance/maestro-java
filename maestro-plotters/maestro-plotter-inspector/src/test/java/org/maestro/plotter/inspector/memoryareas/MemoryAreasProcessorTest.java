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

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MemoryAreasProcessorTest {
    private final String fileName = this.getClass().getResource("/data-ok/memory-areas.csv").getPath();
    private MemoryAreasDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        MemoryAreasProcessor memoryAreasProcessor = new MemoryAreasProcessor();
        MemoryAreasReader memoryAreasReader = new MemoryAreasReader(memoryAreasProcessor);

        dataSet = memoryAreasReader.read(fileName);

    }

    @Test
    public void testRecordCount() {
        assertEquals("The number strictOf heap records don't match", 6, dataSet.getMap().size());
    }

    @Test
    public void testCodeCacheStats() {
        String areaName = "Code Cache";

        Statistics usedStatistics = dataSet.getMap().get(areaName).usedStatistics();
        assertEquals("Unexpected max value for the used " + areaName, 22017088,
                usedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the used " + areaName, 21849728,
                usedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the used " + areaName, 21871575.0893632,
                usedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the used " + areaName, 50619.6737614183,
                usedStatistics.getStandardDeviation(), 0.0001);

        Statistics committedStatistics = dataSet.getMap().get(areaName).committedStatistics();
        assertEquals("Unexpected max value for the committed " + areaName, 22216704,
                committedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the committed " + areaName, 22020096,
                committedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the committed " + areaName, 22045505.2246778,
                committedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the committed " + areaName, 60060.0623959594,
                committedStatistics.getStandardDeviation(), 0.0001);
    }

    @Test
    public void testCompressedClassSpaceStats() {
        String areaName = "Compressed Class Space";

        Statistics usedStatistics = dataSet.getMap().get(areaName).usedStatistics();
        assertEquals("Unexpected max value for the used " + areaName, 4512424,
                usedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the used " + areaName, 4512424,
                usedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the used " + areaName, 4512424,
                usedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the used " + areaName, 0,
                usedStatistics.getStandardDeviation(), 0.0001);

        Statistics committedStatistics = dataSet.getMap().get(areaName).committedStatistics();
        assertEquals("Unexpected max value for the committed " + areaName, 4849664,
                committedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the committed " + areaName, 4849664,
                committedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the committed " + areaName, 4849664,
                committedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the committed " + areaName, 0,
                committedStatistics.getStandardDeviation(), 0.0001);
    }

    @Test
    public void testG1EdenSpaceStats() {
        String areaName = "G1 Eden Space";

        Statistics usedStatistics = dataSet.getMap().get(areaName).usedStatistics();
        assertEquals("Unexpected max value for the used " + areaName, 913309696,
                usedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the used " + areaName, 31457280,
                usedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the used " + areaName, 293243675.9968,
                usedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the used " + areaName, 300196296.298649,
                usedStatistics.getStandardDeviation(), 0.0001);

        Statistics committedStatistics = dataSet.getMap().get(areaName).committedStatistics();
        assertEquals("Unexpected max value for the committed " + areaName, 1042284544,
                committedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the committed " + areaName, 1029701632,
                committedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the committed " + areaName, 1035694287.38505,
                committedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the committed " + areaName, 4065980.7358663,
                committedStatistics.getStandardDeviation(), 0.0001);
    }

    @Test
    public void testG1SurvivorStats() {
        String areaName = "G1 Survivor Space";

        Statistics usedStatistics = dataSet.getMap().get(areaName).usedStatistics();
        assertEquals("Unexpected max value for the used " + areaName, 11534336,
                usedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the used " + areaName, 1048576,
                usedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the used " + areaName, 5840248.30691929,
                usedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the used " + areaName, 3763071.04326821,
                usedStatistics.getStandardDeviation(), 0.0001);

        Statistics committedStatistics = dataSet.getMap().get(areaName).committedStatistics();
        assertEquals("Unexpected max value for the committed " + areaName, 11534336,
                committedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the committed " + areaName, 1048576,
                committedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the committed " + areaName, 5840248.30691929,
                committedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the committed " + areaName, 3763071.04326821,
                committedStatistics.getStandardDeviation(), 0.0001);
    }

    @Test
    public void testG1OldGenStats() {
        String areaName = "G1 Old Gen";

        Statistics usedStatistics = dataSet.getMap().get(areaName).usedStatistics();
        assertEquals("Unexpected max value for the used " + areaName, 551860128,
                usedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the used " + areaName, 543845200,
                usedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the used " + areaName, 546680427.747937,
                usedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the used " + areaName, 3330471.51628771,
                usedStatistics.getStandardDeviation(), 0.0001);

        Statistics committedStatistics = dataSet.getMap().get(areaName).committedStatistics();
        assertEquals("Unexpected max value for the committed " + areaName, 697303040,
                committedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the committed " + areaName, 694157312,
                committedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the committed " + areaName, 695554886.645154,
                committedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the committed " + areaName, 880980.053633853,
                committedStatistics.getStandardDeviation(), 0.0001);
    }

    @Test
    public void testMetaspaceStats() {
        String areaName = "Metaspace";

        Statistics usedStatistics = dataSet.getMap().get(areaName).usedStatistics();
        assertEquals("Unexpected max value for the used " + areaName, 40918152,
                usedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the used " + areaName, 40910504,
                usedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the used " + areaName, 40911223.0586772,
                usedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the used " + areaName, 2131.43621529458,
                usedStatistics.getStandardDeviation(), 0.0001);

        Statistics committedStatistics = dataSet.getMap().get(areaName).committedStatistics();
        assertEquals("Unexpected max value for the committed " + areaName, 41811968,
                committedStatistics.getMax(), 0.0);

        assertEquals("Unexpected min value for the committed " + areaName, 41811968,
                committedStatistics.getMin(), 0.0);

        assertEquals("Unexpected average value for the committed " + areaName, 41811968,
                committedStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected standard deviation value for the committed " + areaName, 0,
                committedStatistics.getStandardDeviation(), 0.0001);
    }


    @Test
    public void testProperties() throws IOException {
        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), MemoryAreasDataSet.DEFAULT_FILENAME);

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(dataSet, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
