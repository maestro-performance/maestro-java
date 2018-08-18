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

package org.maestro.plotter.common;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;
import org.maestro.plotter.common.graph.AbstractPlotter;
import org.maestro.plotter.common.properties.PropertyWriter;

import java.io.File;
import java.io.IOException;

/**
 * Basic/Common plotting class
 * @param <Y> Reader type
 * @param <K> Plotter type
 */
public class BasicPlotter<Y extends ReportReader<?>, K extends AbstractPlotter> {

    private final Y reader;
    private final K plotter;

    /**
     * Constructor
     * @param reader data reader object
     * @param plotter data plotter object
     */
    public BasicPlotter(Y reader, K plotter) {
        this.reader = reader;
        this.plotter = plotter;
    }


    /**
     * A basic plotter wrapping logic
     * @param filename The source filename to plot
     * @param outputFile The output filename (it: the images file)
     * @param propertiesFile An optional plotting properties file
     * @throws MaestroException Wrapping for I/O errors
     * @throws IncompatibleDataSet If the data set is incompatible or invalid
     */
    public void plot(final File filename, final File outputFile, final File propertiesFile) throws MaestroException {
        try {
            Object data = reader.read(filename);

            plotter.plot(data, outputFile);

            if (propertiesFile != null) {
                PropertyWriter propertyWriter = new PropertyWriter();
                propertyWriter.write(data, propertiesFile);
            }
        } catch (IOException e) {
            throw new MaestroException(e);
        }


    }

}
