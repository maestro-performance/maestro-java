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
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.maestro.plotter.latency.common.HdrData;
import org.maestro.plotter.latency.graph.HdrPlotter;
import org.maestro.plotter.latency.properties.HdrPropertyWriter;
import org.maestro.plotter.utils.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.*;

public class SimpleTest {

    private void plot(String fileName) throws Exception {
        // HDR Log Reader
        HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper();

        File sourceFile = new File(fileName);
        Histogram histogram = Util.getAccumulated(sourceFile);

        HdrData hdrData = processorWrapper.convertLog(histogram);

        HdrPlotter plotter = new HdrPlotter(FilenameUtils.removeExtension(sourceFile.getName()));

        plotter.plot(hdrData, sourceFile.getParentFile());

        HdrPropertyWriter hdrPropertyWriter = new HdrPropertyWriter();

        hdrPropertyWriter.postProcess(histogram, sourceFile);

    }


    @Test
    public void testPlot() throws Exception {
        String fileName = this.getClass().getResource("file-01.hdr").getPath();
        plot(fileName);


        String pngFilename99 = FilenameUtils.removeExtension(fileName) + "_99.png";

        File pngFile99 = new File(pngFilename99);
        assertTrue(pngFile99.exists());
        assertTrue(pngFile99.isFile());

        String pngFilename90 = FilenameUtils.removeExtension(fileName) + "_90.png";

        File pngFile90 = new File(pngFilename90);
        assertTrue(pngFile90.exists());
        assertTrue(pngFile90.isFile());

        String pngFilenameAll = FilenameUtils.removeExtension(fileName) + "_all.png";

        File pngFileAll = new File(pngFilenameAll);
        assertTrue(pngFileAll.exists());
        assertTrue(pngFileAll.isFile());


        File propertiesFile = new File(pngFileAll.getParentFile(), "latency.properties");
        assertTrue(propertiesFile.exists());
        assertTrue(propertiesFile.isFile());

        Properties ps = new Properties();

        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            ps.load(inputStream);
        }

        assertEquals("26", ps.getProperty("latency99th"));
        assertEquals("61", ps.getProperty("latency9999th"));
        assertEquals("13", ps.getProperty("latency50th"));
        assertEquals("9916", ps.getProperty("latencyTotalCount"));
        assertEquals("61.0", ps.getProperty("latencyMaxValue"));
    }
}
