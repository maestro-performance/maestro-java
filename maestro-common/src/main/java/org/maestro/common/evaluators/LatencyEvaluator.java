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

/**
 * A evaluator that checks if the latency is greater than a certain threshold
 */
@SuppressWarnings("WeakerAccess")
public abstract class LatencyEvaluator implements Evaluator<Histogram> {
    private final double maxValue;
    private boolean conditionStatus = true;

    /**
     * Constructor
     * @param maxValue maximum value allowed
     */
    protected LatencyEvaluator(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean eval() {
        return this.conditionStatus;
    }

    /**
     * Gets the maximum allowed value
     * @return the maximum allowed value
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Get the mean latency as last recorded
     * @return the last recorded mean latency
     */
    abstract public double getMean();

    /**
     * Mark the evaluation as failed
     */
    protected void setEvalFailed() {
        this.conditionStatus = false;
    }

}
