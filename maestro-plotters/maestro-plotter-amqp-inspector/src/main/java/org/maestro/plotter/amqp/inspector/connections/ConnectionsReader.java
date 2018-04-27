package org.maestro.plotter.amqp.inspector.connections;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.maestro.plotter.common.readers.CsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * A csv router link data reader
 */
public class ConnectionsReader extends CsvReader<ConnectionsDataSet> {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionsReader.class);

    private ConnectionsProcessor connectionsProcessor;

    public ConnectionsReader() {
        this.connectionsProcessor = new ConnectionsProcessor();
    }

    public ConnectionsReader(ConnectionsProcessor connectionsProcessor) {
        this.connectionsProcessor = connectionsProcessor;
    }

    /**
     * Reader of csv file
     * @param reader reader
     * @return readed data
     * @throws IOException implementation specific
     */
    @Override
    protected ConnectionsDataSet readReader(Reader reader) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.RFC4180
                .withCommentMarker('#')
                .withFirstRecordAsHeader()
                .withRecordSeparator(';')
                .withQuote('"')
                .withQuoteMode(QuoteMode.NON_NUMERIC)
                .parse(reader);



        for (CSVRecord record : records) {
            try {
                connectionsProcessor.process(record.get(0), record.get(1), record.get(2), record.get(3), record.get(4),
                        record.get(5), record.get(6), record.get(7), record.get(8), record.get(9), record.get(10),
                        record.get(11), record.get(12), record.get(13), record.get(14));
            } catch (Throwable t) {
                logger.warn("Unable to parse record: {}", t.getMessage(), t);
                continue;
            }
        }

        return connectionsProcessor.getConnectionsDataSet();
    }
}
