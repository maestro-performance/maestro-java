package org.maestro.inspector.activemq.writers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.maestro.common.inspector.types.JVMMemoryInfo;
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
    public void close() throws Exception {
        if (csvPrinter != null) {
            csvPrinter.flush();
            csvPrinter.close();
        }

        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public void write(final LocalDateTime now, final JVMMemoryInfo data) {
        logger.debug("{} Memory Usage: {}", data.getMemoryAreaName(), data);

        try {
            String timestamp = now.format(formatter);

            csvPrinter.printRecord(timestamp, data.getMemoryAreaName(), data.getInitial(), data.getMax(),
                    data.getCommitted(), data.getUsed());
        } catch (IOException e) {
            logger.error("Unable to write record: {}", e.getMessage(), e);
        }
    }
}
