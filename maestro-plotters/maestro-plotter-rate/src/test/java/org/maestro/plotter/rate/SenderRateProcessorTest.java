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

import org.junit.Test;
import org.maestro.plotter.common.properties.PropertyWriter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SenderRateProcessorTest extends CommonRateProcessorTest {

    @Test
    public void testRecordCount() throws Exception {
        RateData rateData = getData("/data-ok/sender.dat");

        final int periodCount = 60;
        super.testRecordCount(periodCount, rateData);
    }

    @Test
    public void testStatistics() throws Exception {
        RateData rateData = getData("/data-ok/sender.dat");

        Statistics statistics = rateData.rateStatistics();

        assertEquals("Unexpected value for the max", 59,
                statistics.getMax(), 0.0000);

        assertEquals("Unexpected value for the min", 0,
                statistics.getMin(), 0.0000);

        /*
         For sender.dat is is 0 because the record contains 0 and it is being
         mathematically strict
         */
        assertEquals("Unexpected value for the geometric mean", 0,
                statistics.getGeometricMean(), 0.0000);

        assertEquals("Unexpected value for the average mean", 29.5,
                statistics.getMean(), 0.0000);
    }

    @Test
    public void testStatisticsNonZero() throws Exception {
        RateData rateData = getData("/data-ok/sender-non-zero.dat");

        Statistics statistics = rateData.rateStatistics();

        assertEquals("Unexpected value for the max", 86400,
                statistics.getMax(), 0.0000);

        assertEquals("Unexpected value for the min", 1,
                statistics.getMin(), 0.0000);

        assertEquals("Unexpected value for the geometric mean", 31787.21266448759,
                statistics.getGeometricMean(), 0.0000);

        assertEquals("Unexpected value for the average mean", 43200.5,
                statistics.getMean(), 0.0000);
    }

    @Test
    public void testProperties() throws Exception {
        RateData rateData = getData("/data-ok/sender.dat");

        File sourceFile = new File(this.getClass().getResource("/data-ok/sender.dat").getPath());
        File outputFile = new File(sourceFile.getParentFile(), "rate.properties");

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(rateData, outputFile);

        assertTrue("The output file does not exist", outputFile.exists());
    }
}
