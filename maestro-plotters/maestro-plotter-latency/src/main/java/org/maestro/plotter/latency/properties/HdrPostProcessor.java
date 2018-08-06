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

import java.io.File;
import java.io.IOException;


/**
 * Post processing of HDR files
 */
public interface HdrPostProcessor {

    /**
     * Post process an HDR file
     * @param histogramFile the file to post-process
     * @param handler An histogram handler to read/save/etc the histogram data
     * @throws Exception implementation-specific
     */
    default void postProcess(final Histogram histogram, final String histogramFile, final HistogramHandler handler) throws Exception {
        handler.handle(histogram, new File(histogramFile));
    }

    /**
     * Post process an HDR file
     * @param histogramFile the file to post-process
     * @param handler An histogram handler to read/save/etc the histogram data
     * @throws Exception implementation-specific
     */
    default void postProcess(final Histogram histogram, final File histogramFile, final HistogramHandler handler) throws Exception {
        handler.handle(histogram, histogramFile);
    }


    /**
     * Save a summary of the analyzed rate data to a properties file named "latency.properties"
     * @param histogramFile the file to post-process
     * @throws IOException implementation-specific
     */
    void postProcess(final Histogram histogram, final File histogramFile) throws Exception;

    /**
     * Save a summary of the analyzed rate data to a properties file named "latency.properties"
     * @param histogramFile the file to post-process
     * @throws IOException implementation-specific
     */
    void postProcess(final Histogram histogram, final String histogramFile) throws Exception;
}
