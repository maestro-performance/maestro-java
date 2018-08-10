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

package org.maestro.common.worker;

import org.maestro.common.stats.PerfStats;

import java.time.Duration;


/**
 * A container for throughput statistics
 */
public class ThroughputStats implements PerfStats {
    private Duration duration;
    private long count;

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getRate() {
        if (duration.getSeconds() == 0) {
            return 0;
        }

        return (double) count / (double) duration.getSeconds();
    }

    @Override
    public String toString() {
        return "ThroughputStats{" +
                "duration=" + duration +
                ", count=" + count +
                '}';
    }
}
