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

package org.maestro.plotter.rate;

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.rate.graph.RatePlotter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class SenderOutOfOrderRateProcessorTest extends CommonRateProcessorTest {
    private final String fileName = this.getClass().getResource("/data-out-of-order/out-of-order-01.csv.gz").getPath();
    private RateData rateData;

    @Before
    public void setUp() throws Exception {
        RateDataProcessor queueProcessor = new RateDataProcessor();
        DefaultRateReader queueReader = new DefaultRateReader(queueProcessor);

        rateData = queueReader.read(fileName);
    }

    @Test
    public void testRecordCount() {
        final int periodCount = 6;
        super.testRecordCount(periodCount, rateData);
    }

    @Test
    public void testStatistics() {

        Statistics statistics = rateData.rateStatistics();

        assertEquals("Unexpected average value for the max", 110.0,
                statistics.getMax(), 0.0000);

        assertEquals("Unexpected average value for the min", 100,
                statistics.getMin(), 0.0000);
    }

    @Test
    public void testOrder() {
        List<Date> periods = rateData.getPeriods();
        Date last = periods.get(0);
        for (int i = 1; i < periods.size(); i++) {
            Date current = periods.get(i);
            if (current.before(last)) {
                fail("Data ordered incorrectly");
            }
        }
    }

    @Test
    public void testProperties() throws IOException {
        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), "rate.properties");

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(rateData, outputFile);

        assertTrue("The output file does not exist", outputFile.exists());
    }

    @Test
    public void testSenderPlot() throws EmptyDataSet, IncompatibleDataSet, IOException {
        RatePlotter ratePlotter = new RatePlotter();

        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), "senderd-rate-01.png");

        ratePlotter.plot(rateData, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
