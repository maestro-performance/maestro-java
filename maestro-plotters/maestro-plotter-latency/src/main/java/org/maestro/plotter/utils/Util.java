package org.maestro.plotter.utils;

import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;


public class Util {
    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static Histogram getAccumulated(final File histogramFile) throws FileNotFoundException {
        Histogram accumulatedHistogram = null;
        DoubleHistogram accumulatedDoubleHistogram = null;

        HistogramLogReader histogramLogReader = new HistogramLogReader(histogramFile);

        int i = 0;
        while (histogramLogReader.hasNext()) {
            EncodableHistogram eh = histogramLogReader.nextIntervalHistogram();

            if (i == 0) {
                if (eh instanceof DoubleHistogram) {
                    accumulatedDoubleHistogram = ((DoubleHistogram) eh).copy();
                    accumulatedDoubleHistogram.reset();
                    accumulatedDoubleHistogram.setAutoResize(true);
                }
                else {
                    accumulatedHistogram = ((Histogram) eh).copy();
                    accumulatedHistogram.reset();
                    accumulatedHistogram.setAutoResize(true);
                }
            }

            logger.debug("Processing histogram from point in time {} to {}",
                    Instant.ofEpochMilli(eh.getStartTimeStamp()), Instant.ofEpochMilli(eh.getEndTimeStamp()));

            if (eh instanceof DoubleHistogram) {
                Objects.requireNonNull(accumulatedDoubleHistogram).add((DoubleHistogram) eh);
            }
            else {
                Objects.requireNonNull(accumulatedHistogram).add((Histogram) eh);
            }

            i++;
        }

        return accumulatedHistogram;
    }


    public static <T> Future<?> asyncPlot(BiConsumer<T, File> plotConsumer, final T data, final File outputDir) {
        ExecutorService plotterService = Executors.newCachedThreadPool();

        return plotterService.submit(() -> plotConsumer.accept(data, outputDir) );
    }

//    public static <T> Future<?> asyncPlot(Consumer<T> plotConsumer, final T data) {
//        ExecutorService plotterService = Executors.newCachedThreadPool();
//
//        return plotterService.submit(() -> plotConsumer.accept(data) );
//    }
}
