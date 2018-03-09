/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.common.evaluators;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A evaluator that checks if the latency is greater than a certain threshold
 */
public class LatencyEvaluator implements Evaluator<Histogram> {
    private static final Logger logger = LoggerFactory.getLogger(LatencyEvaluator.class);
    private double maxValue;
    private boolean conditionStatus = true;

    public LatencyEvaluator(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean eval() {
        return this.conditionStatus;
    }

    @Override
    public synchronized void record(Histogram histogram) {
        if (logger.isTraceEnabled()) {
            logger.trace("Checking the current latency: {} x {}", histogram.getMaxValue(), this.maxValue);
        }

        if (histogram.getMaxValue() > this.maxValue) {
            this.conditionStatus = false;
        }
    }
}
