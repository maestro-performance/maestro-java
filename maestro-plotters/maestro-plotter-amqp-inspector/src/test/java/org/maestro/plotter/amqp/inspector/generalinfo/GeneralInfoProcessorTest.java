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

package org.maestro.plotter.amqp.inspector.generalinfo;

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeneralInfoProcessorTest {
    private final String fileName = this.getClass().getResource("/data-ok/general.csv").getPath();
    private GeneralInfoDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        GeneralInfoProcessor generalInfoProcessor = new GeneralInfoProcessor();
        GeneralInfoReader generalInfoReader = new GeneralInfoReader(generalInfoProcessor);

        dataSet = generalInfoReader.read(fileName);
    }

    @Test
    public void testRecordCount() {
        assertEquals("The number of queue records don't match", 1, dataSet.getMap().size());

        assertEquals("The addresses count for the router records don't match", 5, dataSet.getMap()
                .get("Router.A").getAddressesCount(), 0);

        assertEquals("The connections count for the router records don't match", 11, dataSet.getMap()
                .get("Router.A").getConnectionsCount(), 0);

        assertEquals("The link routes count for the router records don't match", 0, dataSet.getMap()
                .get("Router.A").getLinkRoutesCount(), 0);

        assertEquals("The auto links count for the router records don't match", 0, dataSet.getMap()
                .get("Router.A").getAutoLinksCount(), 0);

    }

    @Test
    public void testGeneralInfoAddressesCountStats() {
        String routerName = "Router.A";

        Statistics queueCountStats = dataSet.getMap().get(routerName).addressesStatistics();
        assertEquals("Unexpected count max value for the router " + routerName, 5,
                queueCountStats.getMax(), 0.0);

        assertEquals("Unexpected count min value for the router " + routerName, 4,
                queueCountStats.getMin(), 0.0);

        assertEquals("Unexpected count average value for the router " + routerName, 4.962947379,
                queueCountStats.getGeometricMean(), 0.0001);

        assertEquals("Unexpected count standard deviation value for the router " + routerName, 0.181020334,
                queueCountStats.getStandardDeviation(), 0.0001);
    }


    @Test
    public void testGeneralInfoConnectionsStats() {
        String routerName = "Router.A";

        Statistics consumerStatistics = dataSet.getMap().get(routerName).connectionsStatistics();
        assertEquals("Unexpected consumer max value for the router " + routerName, 11,
                consumerStatistics.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the router" + routerName, 1,
                consumerStatistics.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the router" + routerName, 10.1549922,
                consumerStatistics.getGeometricMean(), 0.001);

        assertEquals("Unexpected consumer standard deviation value for the router" + routerName, 1.81020334,
                consumerStatistics.getStandardDeviation(), 0.001);
    }

    @Test
    public void testGeneralInfoLinkRoutesStats() {
        String routerName = "Router.A";

        Statistics consumerStatistics = dataSet.getMap().get(routerName).linkRoutesStatistics();
        assertEquals("Unexpected consumer max value for the router " + routerName, 0,
                consumerStatistics.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the router " + routerName, 0,
                consumerStatistics.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the router " + routerName, 0,
                consumerStatistics.getGeometricMean(), 0.001);

        assertEquals("Unexpected consumer standard deviation value for the router " + routerName, 0,
                consumerStatistics.getStandardDeviation(), 0.001);
    }

    @Test
    public void testGeneralInfoAutoLinksStats() {
        String routerName = "Router.A";

        Statistics consumerStatistics = dataSet.getMap().get(routerName).autoLinksStatistics();
        assertEquals("Unexpected consumer max value for the router " + routerName, 0,
                consumerStatistics.getMax(), 0.0);

        assertEquals("Unexpected consumer min value for the router " + routerName, 0,
                consumerStatistics.getMin(), 0.0);

        assertEquals("Unexpected consumer average value for the router " + routerName, 0,
                consumerStatistics.getGeometricMean(), 0.001);

        assertEquals("Unexpected consumer standard deviation value for the router " + routerName, 0,
                consumerStatistics.getStandardDeviation(), 0.001);
    }


    @Test
    public void testProperties() throws IOException {
        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), GeneralInfoData.DEFAULT_FILENAME);

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(dataSet, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
