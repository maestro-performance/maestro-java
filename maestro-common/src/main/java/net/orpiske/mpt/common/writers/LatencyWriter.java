package net.orpiske.mpt.common.writers;

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.HistogramLogWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Writes the latency data in the HdrHistogram data format.
 *
 * @see <a href="https://github.com/HdrHistogram/HdrHistogram/">HdrHistogram</a> documentation
 */
public final class LatencyWriter implements AutoCloseable {

    private final HistogramLogWriter logWriter;

    /**
     * Constructor
     *
     * @param path file path
     * @throws IOException
     */
    public LatencyWriter(final File path) throws IOException {
        logWriter = new HistogramLogWriter(new FileOutputStream(path));
    }

    public void outputLegend(long startedEpochMillis) {
        logWriter.outputComment("[mpt]");
        logWriter.outputLogFormatVersion();
        logWriter.outputStartTime(startedEpochMillis);
        logWriter.outputLegend();
    }

    public void outputIntervalHistogram(EncodableHistogram histogram) {
        logWriter.outputIntervalHistogram(histogram);
    }

    /**
     * Closes the writer
     */
    @Override
    public void close() {
        this.logWriter.close();
    }
}
