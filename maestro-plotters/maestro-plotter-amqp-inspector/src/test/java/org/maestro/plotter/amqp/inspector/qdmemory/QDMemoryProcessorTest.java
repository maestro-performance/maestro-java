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

package org.maestro.plotter.amqp.inspector.qdmemory;

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.amqp.inspector.memory.QDMemoryData;
import org.maestro.plotter.amqp.inspector.memory.QDMemoryDataSet;
import org.maestro.plotter.amqp.inspector.memory.QDMemoryProcessor;
import org.maestro.plotter.amqp.inspector.memory.QDMemoryReader;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QDMemoryProcessorTest {
    private final String fileName = this.getClass().getResource("/data-ok/qdmemory.csv").getPath();
    private QDMemoryDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        QDMemoryProcessor qdMemoryProcessor = new QDMemoryProcessor();
        QDMemoryReader qdMemoryReader = new QDMemoryReader(qdMemoryProcessor);

        dataSet = qdMemoryReader.read(fileName);
    }

    @Test
    public void testRecordCount() {
        assertEquals("The number of queue records don't match", 37, dataSet.getMap().size());

        assertEquals("The held by threads memory for the queue records don't match", 96, dataSet.getMap()
                .get("qd_log_entry_t").getHeldByThreads(), 0);

        assertEquals("The total alloc from heap for the queue records don't match", 96, dataSet.getMap()
                .get("qd_log_entry_t").getTotalAllocFromHeap(), 0);

        assertEquals("The type size for the queue records don't match", 2112, dataSet.getMap()
                .get("qd_log_entry_t").getTypeSize(), 0);
    }

    @Test
    public void testQDMemoryTotalAllocFromHeapStats() {
        String recordName = "qd_log_entry_t";

        Statistics totalAlocatedStats = dataSet.getMap().get(recordName).totalAllocFromHeapStatistics();
        assertEquals("Unexpected consumer max value for the queue " + recordName, 96,
                totalAlocatedStats.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the queue " + recordName, 96,
                totalAlocatedStats.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the queue " + recordName, 96,
                totalAlocatedStats.getGeometricMean(), 0.001);

        assertEquals("Unexpected consumer standard deviation value for the queue " + recordName, 0,
                totalAlocatedStats.getStandardDeviation(), 0.001);
    }

    @Test
    public void testQDMemoryTypeSizeStats() {
        String recordName = "qd_log_entry_t";

        Statistics sizeStats = dataSet.getMap().get(recordName).typeSizeStatistics();
        assertEquals("Unexpected consumer max value for the queue " + recordName, 2112,
                sizeStats.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the queue " + recordName, 2112,
                sizeStats.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the queue " + recordName, 2112,
                sizeStats.getGeometricMean(), 0.001);

        assertEquals("Unexpected consumer standard deviation value for the queue " + recordName, 0,
                sizeStats.getStandardDeviation(), 0.001);
    }

    @Test
    public void testQDMemoryHeldByThreadsStatistics() {
        String recordName = "qd_log_entry_t";

        Statistics totalHeldStats = dataSet.getMap().get(recordName).heldByThreadsStatistics();
        assertEquals("Unexpected count max value for the queue " + recordName, 96,
                totalHeldStats.getMax(), 0.0);

        assertEquals("Unexpected count min value for the queue " + recordName, 96,
                totalHeldStats.getMin(), 0.0);

        assertEquals("Unexpected count average value for the queue " + recordName, 96,
                totalHeldStats.getGeometricMean(), 0.0001);

        assertEquals("Unexpected count standard deviation value for the queue " + recordName, 0,
                totalHeldStats.getStandardDeviation(), 0.0001);
    }

    @Test
    public void testProperties() throws IOException {
        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), QDMemoryData.DEFAULT_FILENAME);

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(dataSet, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
