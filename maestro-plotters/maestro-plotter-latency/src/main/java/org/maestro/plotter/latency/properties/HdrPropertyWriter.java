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

package org.maestro.plotter.latency.properties;

import org.HdrHistogram.Histogram;

import java.io.*;

/**
 * A post processor that writes latency data properties to a file
 */
public class HdrPropertyWriter implements HdrPostProcessor {

    /**
     * Save a summary of the analyzed rate data to a properties file named "latency.properties"
     * @param histogramFile the file to post-process
     * @throws IOException if unable to save
     */
    public void postProcess(final Histogram histogram, final File histogramFile) throws Exception {
        postProcess(histogram, histogramFile, new DefaultHistogramHandler());
    }

    /**
     * Save a summary of the analyzed rate data to a properties file named "latency.properties"
     * @param histogramFile the file to post-process
     * @throws IOException if unable to save
     */
    public void postProcess(final Histogram histogram, final String histogramFile) throws Exception {
        postProcess(histogram, new File(histogramFile));
    }


}
