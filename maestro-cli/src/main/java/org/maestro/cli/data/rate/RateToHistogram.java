/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.maestro.cli.data.rate;

import org.maestro.common.writers.RateWriter;
import org.HdrHistogram.Histogram;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.zip.GZIPInputStream;

public class RateToHistogram {
    private static final DateTimeFormatter dateTimeFormatter;

    static {
        dateTimeFormatter = DateTimeFormatter.ofPattern(RateWriter.TIMESTAMP_FORMAT.replace("\"", ""))
                .withZone(ZoneId.systemDefault());
    }

    public static void convert(final String fileName, PrintStream printStream) throws IOException {
        final Histogram histogram = new Histogram(3);

        appendLatenciesTo(fileName, histogram);
        System.out.println("Latencies reported in MILLISECONDS");
        histogram.outputPercentileDistribution(printStream, 1000d);
    }


    private static void appendLatenciesTo(String fileName, Histogram histogram) throws IOException {
        final boolean compressed = fileName.endsWith(".gz");
        final File file = new File(fileName);
        final InputStream inputStream;

        if (compressed) {
            inputStream = new GZIPInputStream(new FileInputStream(file));
        } else {
            inputStream = new FileInputStream(file);
        }

        try (Reader in = new InputStreamReader(inputStream)) {
            rebuildHistogram(in, histogram, inputStream);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private static void rebuildHistogram(Reader in, Histogram histogram, InputStream inputStream) throws IOException {
        long lines = 0;

        Iterable<CSVRecord> records = CSVFormat.RFC4180
                .withCommentMarker('#')
                .withFirstRecordAsHeader()
                .withRecordSeparator(';')
                .withQuote('"')
                .withQuoteMode(QuoteMode.NON_NUMERIC)
                .parse(in);


        for (CSVRecord record : records) {
            final LocalDateTime start = LocalDateTime.parse(record.get(0), dateTimeFormatter);

            final LocalDateTime end = LocalDateTime.parse(record.get(1), dateTimeFormatter);

            //append it to histogram
            if (start.isAfter(end)) {
                System.err.println("ERROR Line [" + lines + "]:\t" + start + " > " + end);
            } else {
                final long microseconds = ChronoUnit.MICROS.between(start, end);
                histogram.recordValue(microseconds);
            }
        }
    }
}
