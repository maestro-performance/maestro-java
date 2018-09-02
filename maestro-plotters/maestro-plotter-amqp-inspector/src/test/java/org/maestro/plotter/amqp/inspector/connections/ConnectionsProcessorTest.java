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

package org.maestro.plotter.amqp.inspector.connections;

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.common.properties.PropertyWriter;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ConnectionsProcessorTest {
    private final String fileName = this.getClass().getResource("/data-ok/qdmemory.csv").getPath();
    private ConnectionsDataSet dataSet;

    @Before
    public void setUp() throws Exception {
        ConnectionsProcessor connectionsProcessor = new ConnectionsProcessor();
        ConnectionsReader connectionsReader = new ConnectionsReader(connectionsProcessor);

        dataSet = connectionsReader.read(fileName);
    }


    @Test
    public void testProperties() throws IOException {
        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), ConnectionsData.DEFAULT_FILENAME);

        PropertyWriter propertyWriter = new PropertyWriter();

        propertyWriter.write(dataSet, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
