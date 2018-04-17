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

package org.maestro.plotter.inspector.queues;

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueueProcessorTest {
    private final String fileName = this.getClass().getResource("/data-ok/queues.csv").getPath();
    private QueueDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        QueueProcessor queueProcessor = new QueueProcessor();
        QueueReader queueReader = new QueueReader(queueProcessor);

        dataSet = queueReader.read(fileName);
    }

    @Test
    public void testRecordCount() {
        assertEquals("The number of queue records don't match", 7, dataSet.getMap().size());

        assertEquals("The added count of messages for the queue records don't match", 21403875, dataSet.getMap()
                .get("test.performance.queue.4").getAddedCount(), 0);

        assertEquals("The expired count of messages for the queue records don't match", 0, dataSet.getMap()
                .get("test.performance.queue.4").getExpiredCount(), 0);

    }

    @Test
    public void testQueueCountStats() {
        String queueName = "test.performance.queue.4";

        Statistics queueCountStats = dataSet.getMap().get(queueName).countStatistics();
        assertEquals("Unexpected count max value for the queue " + queueName, 82,
                queueCountStats.getMax(), 0.0);

        assertEquals("Unexpected count min value for the queue " + queueName, 0,
                queueCountStats.getMin(), 0.0);

        assertEquals("Unexpected count average value for the queue " + queueName, 0,
                queueCountStats.getGeometricMean(), 0.0001);

        assertEquals("Unexpected count standard deviation value for the queue " + queueName, 24.3700946890141,
                queueCountStats.getStandardDeviation(), 0.0001);
    }


    @Test
    public void testQueueConsumerStats() {
        String queueName = "test.performance.queue.4";

        Statistics consumerStatistics = dataSet.getMap().get(queueName).consumerStatistics();
        assertEquals("Unexpected consumer max value for the queue " + queueName, 1,
                consumerStatistics.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the queue " + queueName, 1,
                consumerStatistics.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the queue " + queueName, 1,
                consumerStatistics.getGeometricMean(), 0.0001);

        assertEquals("Unexpected consumer standard deviation value for the queue " + queueName, 0,
                consumerStatistics.getStandardDeviation(), 0.0001);
    }


    @Test
    public void testProperties() throws IOException {
        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), QueueData.DEFAULT_FILENAME);

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(dataSet, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
