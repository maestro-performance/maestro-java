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

package org.maestro.inspector.amqp.writers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.maestro.common.inspector.types.RouterLinkInfo;
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
import java.util.List;
import java.util.Map;

import static org.maestro.common.LangUtils.closeQuietly;

/**
 * A router link information writer for AMQP Inspector.
 */
public class RouteLinkInfoWriter implements InspectorDataWriter<RouterLinkInfo>, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RouteLinkInfoWriter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private BufferedWriter writer;
    private CSVPrinter csvPrinter;

    public RouteLinkInfoWriter(final File logDir, final String name) throws IOException {
        File outputFile = new File(logDir, name + ".csv");

        writer = Files.newBufferedWriter(Paths.get(outputFile.getPath()), Charset.defaultCharset());
        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Timestamp", "Name", "LinkDir", "OperStatus", "Identity",
                        "DeliveryCount", "UndeliveredCount", "PresettledCount", "UnsettledCount",
                        "ReleasedCount","ModifiedCount", "AcceptedCount", "RejectedCount", "Capacity"));
    }

    /**
     * Close csv printer
     */
    @Override
    public void close() {
        closeQuietly(csvPrinter);
        closeQuietly(writer);
    }

    /**
     * Write single record line into csv file
     * @param now current time
     * @param object one line record
     */
    @SuppressWarnings("unchecked")
    private void write(final LocalDateTime now, final Object object) {
        if (object instanceof Map) {
            final Map<String, Object> routerLinkInfo = (Map<String, Object>) object;

            logger.trace("Router Link information: {}", routerLinkInfo);

            try {
                String timestamp = now.format(formatter);

                csvPrinter.printRecord(timestamp,
                        routerLinkInfo.get("linkName"), routerLinkInfo.get("linkDir"),
                        routerLinkInfo.get("operStatus"), routerLinkInfo.get("identity"),
                        routerLinkInfo.get("deliveryCount"), routerLinkInfo.get("undeliveredCount"),
                        routerLinkInfo.get("presettledCount"), routerLinkInfo.get("unsettledCount"),
                        routerLinkInfo.get("releasedCount"), routerLinkInfo.get("modifiedCount"),
                        routerLinkInfo.get("acceptedCount"), routerLinkInfo.get("rejectedCount"),
                        routerLinkInfo.get("capacity"));


            } catch (IOException e) {
                logger.error("Unable to write record: {}", e.getMessage(), e);
            }
        }
        else {
            logger.warn("Invalid value type for router link");
        }
    }

    /**
     * Write collected data
     * @param now current time
     * @param data data for print
     */
    @SuppressWarnings("unchecked")
    @Override
    public void write(final LocalDateTime now, final RouterLinkInfo data) {
        logger.trace("Router link information: {}", data);

        List<Map<String, Object>> queueProperties = data.getRouterLinkProperties();

        queueProperties.forEach(map -> write(now, map));
    }
}
