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
import org.maestro.common.inspector.types.QueueInfo;
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
import java.util.Map;

import static org.maestro.common.LangUtils.closeQuietly;


public class QueueInfoWriter implements InspectorDataWriter<QueueInfo>, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(QueueInfoWriter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private BufferedWriter writer;
    private CSVPrinter csvPrinter;

    public QueueInfoWriter(final File logDir, final String name) throws IOException {
        File outputFile = new File(logDir, name + ".csv");

        writer = Files.newBufferedWriter(Paths.get(outputFile.getPath()), Charset.defaultCharset());
        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Timestamp", "Name", "MessagesAdded", "MessageCount", "MessagesAcknowledged",
                        "MessagesExpired", "ConsumerCount"));
    }

    @Override
    public void close() {
        closeQuietly(csvPrinter);
        closeQuietly(writer);
    }


    private void write(final LocalDateTime now, final String key, final Object object) {
        if (object instanceof Map) {
            final Map<?, ?> queueProperties = (Map<?, ?>) object;
            logger.trace("Queue information: {}", queueProperties);

            try {
                String timestamp = now.format(formatter);

                csvPrinter.printRecord(timestamp,
                        queueProperties.get("Name"), queueProperties.get("MessagesAdded"),
                        queueProperties.get("MessageCount"), queueProperties.get("MessagesAcknowledged"),
                        queueProperties.get("MessagesExpired"), queueProperties.get("ConsumerCount"));
                csvPrinter.flush();
            } catch (IOException e) {
                logger.error("Unable to write record: {}", e.getMessage(), e);
            }
        }
        else {
            logger.warn("Invalid value type for {}", key);
        }
    }

    @Override
    public void write(final LocalDateTime now, final QueueInfo data) {
        logger.trace("Queue information: {}", data);

        Map<String, Object> queueProperties = data.getQueueProperties();

        queueProperties.forEach((key, value) -> write(now, key, value));
    }
}
