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
import org.maestro.common.inspector.types.GeneralInfo;
import org.maestro.common.test.InspectorProperties;
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
public class GeneralInfoWriter implements InspectorDataWriter<GeneralInfo>, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GeneralInfoWriter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private BufferedWriter writer;
    private CSVPrinter csvPrinter;

    public GeneralInfoWriter(final File logDir, final String name) throws IOException {
        File outputFile = new File(logDir, name + ".csv");

        writer = Files.newBufferedWriter(Paths.get(outputFile.getPath()), Charset.defaultCharset());
        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Timestamp", "Name", "Version", "Mode", "LinkRoutes", "AutoLinks", "Links", "Nodes",
                        "Addresses", "Connections"));
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

            logger.trace("General information: {}", routerLinkInfo);

            try {
                String timestamp = now.format(formatter);

                csvPrinter.printRecord(timestamp,
                        routerLinkInfo.get("name"), routerLinkInfo.get("version"),
                        routerLinkInfo.get("mode"), routerLinkInfo.get("linkRouteCount"),
                        routerLinkInfo.get("autoLinkCount"), routerLinkInfo.get("linkCount"),
                        routerLinkInfo.get("nodeCount"), routerLinkInfo.get("addrCount"),
                        routerLinkInfo.get("connectionCount"));


            } catch (IOException e) {
                logger.error("Unable to write record: {}", e.getMessage(), e);
            }
        }
        else {
            logger.warn("Invalid value type for general info");
        }
    }

    /**
     * Write collected data
     * @param now current time
     * @param data data for print
     */
    @SuppressWarnings("unchecked")
    @Override
    public void write(final LocalDateTime now, final GeneralInfo data) {
        logger.trace("Router link information: {}", data);

        List<Map<String, Object>> queueProperties = data.getGeneralProperties();

        queueProperties.forEach(map -> write(now, map));
    }

    @SuppressWarnings("unchecked")
    public void write(InspectorProperties inspectorProperties, final Object object){
        if (object instanceof GeneralInfo) {
            final Map<String, Object> generalInfo = ((GeneralInfo) object).getGeneralProperties().get(0);

            logger.trace("Router Link information: {}", generalInfo);

            Runtime runtime = Runtime.getRuntime();

            inspectorProperties.setSystemCpuCount(runtime.availableProcessors());
            inspectorProperties.setSystemMemory(runtime.totalMemory());

            inspectorProperties.setOperatingSystemName(System.getProperty("os.name"));
            inspectorProperties.setOperatingSystemArch(System.getProperty("os.arch"));
            inspectorProperties.setOperatingSystemVersion(System.getProperty("os.version"));

            inspectorProperties.setJvmName(System.getProperty("java.vm.name"));
            inspectorProperties.setJvmVersion(System.getProperty("java.specification.version"));

            inspectorProperties.setProductName("Interconnect");
            inspectorProperties.setProductVersion((String) generalInfo.get("version"));

        }
        else {
            logger.warn("Invalid value type for general info");
        }
    }
}
