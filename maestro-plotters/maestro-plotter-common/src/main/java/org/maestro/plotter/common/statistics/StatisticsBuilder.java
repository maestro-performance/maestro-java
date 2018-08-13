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

package org.maestro.plotter.common.statistics;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.stream.DoubleStream;

/**
 * A builder for statistics containers
 */
public class StatisticsBuilder {

    /**
     * Having zeros on the data set may render some of the calculations invalid (ie.: the Geometric mean). This method
     * enforces strict mathematical correctness by default, but it can lead to some situations
     * where the geometric mean for rate, CPU and RSS is zero. Setting strict to false disables
     * this behavior.
     * @param stream Input stream
     * @return A statistics container for the input stream
     */
    public static Statistics strictOf(DoubleStream stream) {
        SummaryStatistics summaryStatistics = new SummaryStatistics();

        stream.filter(tmp -> tmp > 0)
                .forEach(summaryStatistics::addValue);


        return new Statistics(summaryStatistics);
    }


    /**
     * A non-strict statistics builder
     * @param stream Input stream
     * @return A statistics container for the input stream
     */
    public static Statistics of(DoubleStream stream) {
        SummaryStatistics summaryStatistics = new SummaryStatistics();

        stream.forEach(summaryStatistics::addValue);


        return new Statistics(summaryStatistics);
    }
}
