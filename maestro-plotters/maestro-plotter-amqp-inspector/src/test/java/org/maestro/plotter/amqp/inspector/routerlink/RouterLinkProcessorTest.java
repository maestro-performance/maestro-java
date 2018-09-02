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

package org.maestro.plotter.amqp.inspector.routerlink;

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RouterLinkProcessorTest {
    private final String fileName = this.getClass().getResource("/data-ok/routerLink.csv").getPath();
    private RouterLinkDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        RouterLinkProcessor routerLinkProcessor = new RouterLinkProcessor();
        RouterLinkReader routerLinkReader = new RouterLinkReader(routerLinkProcessor);

        dataSet = routerLinkReader.read(fileName);
    }

    @Test
    public void testRecordCount() {
        assertEquals("The number of queue records don't match", 13, dataSet.getMap().size());

        assertEquals("The accepted count of messages for the queue records don't match", 0, dataSet.getMap()
                .get("qpid-jms:receiver:ID:9804fae5-acea-411c-9e35-beac6cd4c36b:1:1:1:test").getAcceptedCount(), 0);

        assertEquals("The delivered count of messages for the queue records don't match", 818645, dataSet.getMap()
                .get("qpid-jms:receiver:ID:9804fae5-acea-411c-9e35-beac6cd4c36b:1:1:1:test").getDeliveredCount(), 0);

        assertEquals("The released count of messages for the queue records don't match", 0, dataSet.getMap()
                .get("qpid-jms:receiver:ID:9804fae5-acea-411c-9e35-beac6cd4c36b:1:1:1:test").getReleasedCount(), 0);

        assertEquals("The undelivered count of messages for the queue records don't match", 34, dataSet.getMap()
                .get("qpid-jms:receiver:ID:9804fae5-acea-411c-9e35-beac6cd4c36b:1:1:1:test").getUndeliveredCount(), 0);

    }

    // TODO jstejska: check following methods
    @Test
    public void testRouterLinkAcceptedCountStats() {
        String queueName = "qpid-jms:receiver:ID:9804fae5-acea-411c-9e35-beac6cd4c36b:1:1:1:test";

        Statistics queueCountStats = dataSet.getMap().get(queueName).acceptedStatistics();
        assertEquals("Unexpected count max value for the queue " + queueName, 0,
                queueCountStats.getMax(), 0.0);

        assertEquals("Unexpected count min value for the queue " + queueName, 0,
                queueCountStats.getMin(), 0.0);

        assertEquals("Unexpected count average value for the queue " + queueName, 0,
                queueCountStats.getGeometricMean(), 0.0001);

        assertEquals("Unexpected count standard deviation value for the queue " + queueName, 0,
                queueCountStats.getStandardDeviation(), 0.0001);
    }


    @Test
    public void testRouterLinkDeliveredStats() {
        String queueName = "qpid-jms:receiver:ID:9804fae5-acea-411c-9e35-beac6cd4c36b:1:1:1:test";

        Statistics consumerStatistics = dataSet.getMap().get(queueName).deliveredStatistics();
        assertEquals("Unexpected consumer max value for the queue " + queueName, 818645,
                consumerStatistics.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the queue " + queueName, 4873,
                consumerStatistics.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the queue " + queueName, 287250.0223501416,
                consumerStatistics.getGeometricMean(), 0.001);

        assertEquals("Unexpected consumer standard deviation value for the queue " + queueName, 241779.17554071322,
                consumerStatistics.getStandardDeviation(), 0.001);
    }

    @Test
    public void testRouterLinkUndeliveredStats() {
        String queueName = "qpid-jms:receiver:ID:9804fae5-acea-411c-9e35-beac6cd4c36b:1:1:1:test";

        Statistics consumerStatistics = dataSet.getMap().get(queueName).undeliveredStatistics();
        assertEquals("Unexpected consumer max value for the queue " + queueName, 34,
                consumerStatistics.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the queue " + queueName, 0,
                consumerStatistics.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the queue " + queueName, 0,
                consumerStatistics.getGeometricMean(), 0.001);

        assertEquals("Unexpected consumer standard deviation value for the queue " + queueName, 4.569454596065611,
                consumerStatistics.getStandardDeviation(), 0.001);
    }

    @Test
    public void testRouterLinkReleasedStats() {
        String queueName = "qpid-jms:receiver:ID:9804fae5-acea-411c-9e35-beac6cd4c36b:1:1:1:test";

        Statistics consumerStatistics = dataSet.getMap().get(queueName).releasedStatistics();
        assertEquals("Unexpected consumer max value for the queue " + queueName, 0,
                consumerStatistics.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the queue " + queueName, 0,
                consumerStatistics.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the queue " + queueName, 0,
                consumerStatistics.getGeometricMean(), 0.001);

        assertEquals("Unexpected consumer standard deviation value for the queue " + queueName, 0,
                consumerStatistics.getStandardDeviation(), 0.001);
    }


    @Test
    public void testProperties() throws IOException {
        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), RouterLinkData.DEFAULT_FILENAME);

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(dataSet, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
