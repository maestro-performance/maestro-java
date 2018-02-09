package org.maestro.common.writers;

import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.HistogramLogWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes the latency data in the HdrHistogram data format.
 *
 * @see <a href="https://github.com/HdrHistogram/HdrHistogram/">HdrHistogram</a> documentation
 */
public final class LatencyWriter implements AutoCloseable {

    private final HistogramLogWriter logWriter;
    private final OutputStream out;

    /**
     * Constructor
     *
     * @param path file path
     * @throws IOException
     */
    public LatencyWriter(final File path) throws IOException {
        out = new FileOutputStream(path);
        logWriter = new HistogramLogWriter(this.out);
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
        try {
            //to be sure everything has been correctly written (not necessary)
            this.out.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            this.logWriter.close();
        }
    }
}
