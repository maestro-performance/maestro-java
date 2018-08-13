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

package org.maestro.tests.utils;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.duration.DurationTime;
import org.maestro.common.duration.TestDuration;

/**
 * Test completion time calculator
 */
public class CompletionTime {
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();


    private CompletionTime() {}

    /**
     * Estimates the test completion time
     * @param duration test duration object
     * @param rate test rate
     * @return the estimated duration in seconds
     */
    public static long estimate(final TestDuration duration, final long rate) {
        long ret;

        if (duration instanceof DurationTime) {
            final long defaultMultiplier = 2;
            long multiplier = config.getLong("duration.time.wait.multiplier", defaultMultiplier);

            ret = (duration.getNumericDuration() * multiplier) + 30;
        }
        else {
            final long defaultBase = 90;
            ret = config.getLong("duration.count.wait.base", defaultBase);

            if (rate > 0) {
                ret += Math.round((double) duration.getNumericDuration() / (double) rate);
            }
            else {
                ret = ret * 7;
            }
        }

        return ret;
    }

    public static long getDeadline() {
        return config.getLong("worker.active.deadline.max", 120000) / 1000;
    }
}
