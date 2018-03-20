package org.maestro.inspector.activemq.writers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.maestro.common.inspector.types.QueueInfo;
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
import java.util.Map;

public class QueueInfoWriter implements InspectorDataWriter<QueueInfo>, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(QueueInfoWriter.class);
    private BufferedWriter writer;
    private CSVPrinter csvPrinter;

    public QueueInfoWriter(final File logDir, final String name) throws IOException {
        File outputFile = new File(logDir, name + ".csv");

        writer = Files.newBufferedWriter(Paths.get(outputFile.getPath()), Charset.defaultCharset());
        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Name", "MessagesAdded", "MessageCount", "MessagesAcknowledged", "MessagesExpired",
                        "ConsumerCount"));
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


    public void write(final LocalDateTime now, final String key, final Object object) {
        if (object instanceof Map) {
            final Map<String, Object> queueProperties = (Map) object;
            logger.debug("Queue information: {}", queueProperties);

            try {
                csvPrinter.printRecord(
                        queueProperties.get("Name"), queueProperties.get("MessagesAdded"),
                        queueProperties.get("MessageCount"), queueProperties.get("MessagesAcknowledged"),
                        queueProperties.get("MessagesExpired"), queueProperties.get("ConsumerCount"));
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
        logger.debug("Queue information: {}", data);

        Map<String, Object> queueProperties = data.getQueueProperties();

        queueProperties.forEach((key, value) -> write(now, key, value));
    }
}
