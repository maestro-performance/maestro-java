/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orpiske.mpt.data.rate;

import net.orpiske.mpt.common.writers.RateWriter;
import org.HdrHistogram.Histogram;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.zip.GZIPInputStream;

public class RateToHistogram {
    public static void convert(final String fileName, PrintStream printStream) throws IOException {
        final Histogram histogram = new Histogram(3);

        appendLatenciesTo(fileName, histogram);
        System.out.println("Latencies reported in MILLISECONDS");
        histogram.outputPercentileDistribution(printStream, 1000d);
    }

    private static long appendLatenciesTo(String fileName, Histogram histogram) throws IOException {
        final boolean compressed = fileName.endsWith(".gz");
        final File file = new File(fileName);
        final InputStream inputStream;

        if (compressed) {
            inputStream = new GZIPInputStream(new FileInputStream(file));
        } else {
            inputStream = new FileInputStream(file);
        }

        final byte[] readLineBuffer = new byte[RateWriter.ESTIMATED_LINE_LENGTH];
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(RateWriter.TIMESTAMP_FORMAT).withZone(ZoneId.systemDefault());
        long lines = 0;

        try (InputStream stream = inputStream) {
            //TODO handle the first return value
            stream.skip(RateWriter.HEADER_LENGTH);
            lines++;
            boolean finished = false;
            final int timestampLength = RateWriter.TIMESTAMP_FORMAT.length();
            final StringBuilder timestamp = new StringBuilder(timestampLength);
            while (!finished) {
                final int read = stream.read(readLineBuffer);
                finished = read == -1 || read == 1;
                if (!finished) {
                    //skip the first \n
                    assert readLineBuffer[0] == '\n';
                    //parse the first timestamp
                    timestamp.setLength(0);
                    int offset = 1;
                    for (int i = 0; i < timestampLength; i++) {
                        final char ch = (char) readLineBuffer[offset + i];
                        timestamp.append(ch);
                    }
                    final LocalDateTime start = LocalDateTime.parse(timestamp, dateTimeFormatter);
                    offset += timestampLength;
                    assert readLineBuffer[offset] == RateWriter.SEPARATOR;
                    //skip separator
                    offset++;
                    timestamp.setLength(0);
                    //parse the second timestamp
                    for (int i = 0; i < timestampLength; i++) {
                        final char ch = (char) readLineBuffer[offset + i];
                        timestamp.append(ch);
                    }
                    final LocalDateTime end = LocalDateTime.parse(timestamp, dateTimeFormatter);
                    //append it to histogram
                    if (start.isAfter(end)) {
                        System.err.println("ERROR Line [" + lines + "]:\t" + start + " > " + end);
                    } else {
                        //can be added to the histogram
                        final long microseconds = ChronoUnit.MICROS.between(start, end);
                        histogram.recordValue(microseconds);
                    }
                    lines++;
                }
            }
            return lines;
        }
    }
}
