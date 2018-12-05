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

package org.maestro.plotter.latency;

import org.HdrHistogram.Histogram;
import org.junit.Test;
import org.maestro.plotter.latency.common.HdrData;
import org.maestro.plotter.latency.properties.DefaultHistogramHandler;
import org.maestro.plotter.latency.properties.HdrPropertyWriter;
import org.maestro.plotter.utils.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleTestWithUnitRatio {

    @Test
    public void testSimpleFileWithUnitRatio() throws Exception {
        String fileName = this.getClass().getResource("file-01.hdr").getPath();

        // HDR Log Reader
        HdrLogProcessorWrapper processorWrapper = new DefaultHdrLogProcessorWrapper(10.0);

        File sourceFile = new File(fileName);
        Histogram histogram = Util.getAccumulated(sourceFile);

        HdrData hdrData = processorWrapper.convertLog(histogram);

        HdrPropertyWriter hdrPropertyWriter = new HdrPropertyWriter();

        hdrPropertyWriter.postProcess(histogram, sourceFile, new DefaultHistogramHandler(10.0));

        File propertiesFile = new File(sourceFile.getParentFile(), "latency.properties");
        assertTrue(propertiesFile.exists());
        assertTrue(propertiesFile.isFile());

        Properties ps = new Properties();

        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            ps.load(inputStream);
        }

        assertEquals("2", ps.getProperty("latency99th"));
        assertEquals("6", ps.getProperty("latency9999th"));
        assertEquals("1", ps.getProperty("latency50th"));
        assertEquals("9916", ps.getProperty("latencyTotalCount"));
        assertEquals("6.1", ps.getProperty("latencyMaxValue"));
    }
}
