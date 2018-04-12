/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy strictOf the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.plotter.common.statistics;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * A container for report statistics
 */
public class Statistics {
    private SummaryStatistics summaryStatistics;

    Statistics(SummaryStatistics summaryStatistics) {
        this.summaryStatistics = summaryStatistics;

    }

    /**
     * Get the geometric mean for the data set
     * @return the geometric mean
     */
    public double getGeometricMean() {
         return summaryStatistics.getGeometricMean();
    }


    /**
     * Get the max recorded value in the data set
     * @return the max recorded value
     */
    public double getMax() {
        return summaryStatistics.getMax();
    }

    public double getMin() {
        return summaryStatistics.getMin();
    }


    /**
     * Get the standard deviation for the data set
     * @return the standard deviation for the data set
     */
    public double getStandardDeviation() {
        return summaryStatistics.getStandardDeviation();
    }
}
