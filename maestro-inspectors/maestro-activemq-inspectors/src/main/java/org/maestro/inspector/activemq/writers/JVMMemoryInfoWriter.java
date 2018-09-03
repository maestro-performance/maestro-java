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

package org.maestro.inspector.activemq.writers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.maestro.common.inspector.types.JVMMemoryInfo;
import org.maestro.common.io.data.writers.InspectorDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.maestro.common.LangUtils.closeQuietly;

public class JVMMemoryInfoWriter implements InspectorDataWriter<JVMMemoryInfo>, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(JVMMemoryInfoWriter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private BufferedWriter writer;
    private CSVPrinter csvPrinter;

    public JVMMemoryInfoWriter(final File logDir, final String name) throws IOException {
        File outputFile = new File(logDir, name + ".csv");

         writer = Files.newBufferedWriter(Paths.get(outputFile.getPath()), Charset.defaultCharset());
         csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Timestamp", "Name", "Initial", "Max", "Committed", "Used"));
    }

    @Override
    public void close() {
        closeQuietly(csvPrinter);
        closeQuietly(writer);
    }

    @Override
    public void write(final LocalDateTime now, final JVMMemoryInfo data) {
        if (logger.isTraceEnabled()) {
            logger.trace("{} Memory Usage: {}", data.getMemoryAreaName(), data);
        }

        try {
            String timestamp = now.format(formatter);

            csvPrinter.printRecord(timestamp, data.getMemoryAreaName(), data.getInitial(), data.getMax(),
                    data.getCommitted(), data.getUsed());
            csvPrinter.flush();
        } catch (IOException e) {
            logger.error("Unable to write record: {}", e.getMessage(), e);
        }
    }
}
