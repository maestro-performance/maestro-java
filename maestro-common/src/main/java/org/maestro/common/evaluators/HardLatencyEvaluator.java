package org.maestro.common.evaluators;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A latency evaluator that causes the test to fail if the maximum recorded latency is greater
 * than the threshold value
 */
public class HardLatencyEvaluator extends LatencyEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(HardLatencyEvaluator.class);
    private double mean;

    /**
     * Constructor
     * @param maxValue latency threshold
     */
    public HardLatencyEvaluator(double maxValue) {
        super(maxValue);
    }

    @Override
    public synchronized void record(final Histogram histogram) {
        long maxRecordedValue = histogram.getMaxValue();

        if (logger.isTraceEnabled()) {
            logger.trace("Checking the current latency: {} x {}", maxRecordedValue, getMaxValue());
        }

        if (maxRecordedValue > getMaxValue()) {
            logger.warn("The maximum recorded latency ({} us) exceeds the maximum allowed value of ({} us)",
                    maxRecordedValue, getMaxValue());

            setEvalFailed();
        }

        mean = histogram.getMean();
    }

    @Override
    public double getMean() {
        return mean;
    }
}
