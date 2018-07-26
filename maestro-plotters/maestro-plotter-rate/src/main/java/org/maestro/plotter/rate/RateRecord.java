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

package org.maestro.plotter.rate;

import org.maestro.plotter.common.InstantRecord;

import java.time.Instant;

/**
 * Rate information for a given timestamp of time
 */
class RateRecord implements Comparable<RateRecord>, InstantRecord {
    private final Instant timestamp;
    private long count;

    public RateRecord(Instant timestamp, long count) {
        this.timestamp = timestamp;
        this.count = count;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    @Override
    public int compareTo(final RateRecord rateRecord) {
        return this.getTimestamp().compareTo(rateRecord.getTimestamp());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RateRecord rateRecord = (RateRecord) o;

        return timestamp.equals(rateRecord.timestamp);
    }

    @Override
    public int hashCode() {
        return timestamp.hashCode();
    }

    @Override
    public String toString() {
        return "RateRecord{" +
                "timestamp=" + timestamp +
                ", count=" + count +
                '}';
    }
}
