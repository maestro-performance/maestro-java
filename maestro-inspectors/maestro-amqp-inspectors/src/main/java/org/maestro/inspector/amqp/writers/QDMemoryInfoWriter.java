package org.maestro.inspector.amqp.writers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.maestro.common.inspector.types.QDMemoryInfo;
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
import java.util.List;
import java.util.Map;

/**
 * A memory information writer for AMQP Inspector.
 */
public class QDMemoryInfoWriter implements InspectorDataWriter<QDMemoryInfo>, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionsInfoWriter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private BufferedWriter writer;
    private CSVPrinter csvPrinter;

    public QDMemoryInfoWriter(final File logDir, final String name) throws IOException {
        File outputFile = new File(logDir, name + ".csv");

        writer = Files.newBufferedWriter(Paths.get(outputFile.getPath()), Charset.defaultCharset());
        csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Timestamp", "Name", "Size", "Batch", "Thread-max", "Total", "In-threads",
                        "Rebal-in", "Rebal-out", "totalFreeToHeap", "globalFreeListMax"));
    }

    /**
     * Close csv printer
     * @throws Exception implementation specific
     */
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

    /**
     * Write single record line into csv file
     * @param now current time
     * @param object one line record
     */
    @SuppressWarnings("unchecked")
    private void write(final LocalDateTime now, final Object object) {
        if (object instanceof Map) {
            final Map<String, Object> ConnectionsInfo = (Map<String, Object>) object;

            logger.trace("Memory information: {}", ConnectionsInfo);

            try {
                String timestamp = now.format(formatter);

                csvPrinter.printRecord(timestamp,
                        ConnectionsInfo.get("typeName"), ConnectionsInfo.get("typeSize"),
                        ConnectionsInfo.get("transferBatchSize"), ConnectionsInfo.get("localFreeListMax"),
                        ConnectionsInfo.get("totalAllocFromHeap"), ConnectionsInfo.get("heldByThreads"),
                        ConnectionsInfo.get("batchesRebalancedToThreads"), ConnectionsInfo.get("batchesRebalancedToGlobal"),
                        ConnectionsInfo.get("totalFreeToHeap"), ConnectionsInfo.get("globalFreeListMax"));


            } catch (IOException e) {
                logger.error("Unable to write record: {}", e.getMessage(), e);
            }
        }
        else {
            logger.warn("Invalid value type for Memory");
        }
    }

    /**
     * Write collected data
     * @param now current time
     * @param data data for print
     */
    @SuppressWarnings("unchecked")
    @Override
    public void write(final LocalDateTime now, final QDMemoryInfo data) {
        logger.trace("Memory information: {}", data);

        List connectionProperties = data.getQDMemoryInfoProperties();

        connectionProperties.forEach(map -> write(now, map));
    }
}
