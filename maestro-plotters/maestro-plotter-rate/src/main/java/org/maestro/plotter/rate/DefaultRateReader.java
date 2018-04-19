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

package org.maestro.plotter.rate;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.maestro.plotter.common.readers.CompressedCsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * A reader for the rate information
 */
public class DefaultRateReader extends CompressedCsvReader<RateData> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRateReader.class);
    private RateDataProcessor processor;

    public DefaultRateReader(final RateDataProcessor processor) {
        this.processor = processor;

        logger.debug("Reading records using the default rate reader");
    }

    @Override
    protected RateData readReader(Reader reader) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.RFC4180
                .withCommentMarker('#')
                .withFirstRecordAsHeader()
                .withRecordSeparator(',')
                .withQuote('"')
                .withQuoteMode(QuoteMode.NON_NUMERIC)
                .parse(reader);

        for (CSVRecord record : records) {
            try {
                processor.process(record.get(0), record.get(1));
            } catch (Exception e) {
                logger.warn("Unable to parse record: {}", e.getMessage(), e);
            }
        }

        return processor.getRateData();
    }
}
