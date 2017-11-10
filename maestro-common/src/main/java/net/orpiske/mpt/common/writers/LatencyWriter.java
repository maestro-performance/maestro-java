package net.orpiske.mpt.common.writers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import org.HdrHistogram.*;

/**
 * Writes the latency data in the HdrHistogram data format.
 *
 * @see https://github.com/HdrHistogram/HdrHistogram/
 */
public class LatencyWriter {
    private OutputStream fileStream = null;

    private static Histogram histogram = new Histogram(3600000000000L, 3);
    private HistogramLogWriter logWriter;

    private LatencyDataConverter converter = new StringLatencyConverter();
    private Instant start;
    private Instant end;

    /**
     * Constructor
     * @param path file path
     * @throws IOException
     */
    public LatencyWriter(final File path) throws IOException {
        fileStream = new FileOutputStream(path);
        logWriter = new HistogramLogWriter(fileStream);

        histogram.reset();

        start = Instant.now();
    }

    /**
     * Gets the latency data converter
     * @return
     */
    public LatencyDataConverter getConverter() {
        return converter;
    }


    /**
     * Sets a latency data converter. The converter can be used to adjust
     * worker-specific data (ie.: some benchmark tools may use milliseconds instead of microseconds)
     * @param converter
     */
    public void setConverter(LatencyDataConverter converter) {
        this.converter = converter;
    }


    public void writeLine(long latency) {
        histogram.recordValue(latency);
    }

    /**
     * Write a line in the performance data report
     * @param latency to record/write
     * @throws IOException
     */
    public void writeLine(final String latency) throws IOException {
        writeLine(converter.convert(latency));
    }


    /**
     * Closes the writer
     */
    public void close() {
        try {
            end = Instant.now();

            logWriter.outputIntervalHistogram(start.getEpochSecond(), end.getEpochSecond(), histogram);

            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
