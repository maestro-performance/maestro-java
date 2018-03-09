package org.maestro.common.evaluators;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A latency evaluator that causes the test to fail if the recorded latency at a given percentile is greater
 * than the threshold value
 */
public class SoftLatencyEvaluator extends LatencyEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(SoftLatencyEvaluator.class);
    private final double defaultPercentile;

    /**
     * Constructor
     * @param maxValue maximum latency value
     * @param defaultPercentile percentile to collect the latency
     */
    public SoftLatencyEvaluator(double maxValue, double defaultPercentile) {
        super(maxValue);

        this.defaultPercentile = defaultPercentile;
    }

    @Override
    public synchronized void record(final Histogram histogram) {
        long maxRecordedValue = histogram.getValueAtPercentile(this.defaultPercentile);


        if (logger.isTraceEnabled()) {
            logger.trace("Checking the current latency: {} x {}", maxRecordedValue, getMaxValue());
        }

        if (maxRecordedValue > getMaxValue()) {
            logger.warn("The maximum recorded latency ({} us) exceeds the maximum allowed value of ({} us) at percentile",
                    maxRecordedValue, getMaxValue());

            setConditionStatus(false);
        }
    }
}
