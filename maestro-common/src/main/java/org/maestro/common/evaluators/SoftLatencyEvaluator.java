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
    private double mean;

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

            setEvalFailed();
        }

        mean = histogram.getMean();
    }

    @Override
    public double getMean() {
        return mean;
    }
}
