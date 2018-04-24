package org.maestro.inspector.amqp.writers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.maestro.common.inspector.types.QueueInfo;
import org.maestro.common.inspector.types.RouterLinkInfo;
import org.maestro.common.writers.InspectorDataWriter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteLinkInfoWriter implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RouteLinkInfoWriter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private BufferedWriter writer;
    private CSVPrinter csvPrinter;

    public RouteLinkInfoWriter(final File logDir, final String name) throws IOException {
        File outputFile = new File(logDir, name + ".csv");

        System.out.println(outputFile.getPath());

        writer = Files.newBufferedWriter(Paths.get(outputFile.getPath()), Charset.defaultCharset());
        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Timestamp", "Name", "MessagesAdded", "MessageCount", "MessagesAcknowledged",
                        "MessagesExpired", "ConsumerCount"));
    }

    @Override
    public void close() throws Exception {
        if (csvPrinter != null) {
            csvPrinter.flush();
            csvPrinter.close();
        }

        if (writer != null) {
            writer.close();
        }
    }


    private void write(final LocalDateTime now, final Object object) {
        if (object instanceof Map) {
//            final Map<String, Object> queueProperties = (Map<String, Object>) object;

            final Map<String, Object> routerLinkInfo = (Map<String, Object>) object;

            logger.debug("Router Link information: {}", routerLinkInfo);

            try {
                String timestamp = now.format(formatter);

//                for (Map<Object, Object> item: routerLinkInfo) {
//                }

                csvPrinter.printRecord(timestamp,
                        routerLinkInfo.get("linkName"), routerLinkInfo.get("capacity"),
                        routerLinkInfo.get("acceptedCount"), routerLinkInfo.get("rejectedCount"),
                        routerLinkInfo.get("linkType"), routerLinkInfo.get("deliveryCount"));


            } catch (IOException e) {
                logger.error("Unable to write record: {}", e.getMessage(), e);
            }
        }
        else {
            logger.warn("Invalid value type for router link");
        }
    }

//    @Override
    public void write(final LocalDateTime now, final RouterLinkInfo data) {
        logger.debug("Queue information: {}", data);

        List queueProperties = data.getRouterLinkProperties();

        queueProperties.forEach(map -> write(now, map));

//        queueProperties.forEach((key, value) -> write(now, key, value));
    }
}
