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

package org.maestro.plotter.inspector.common;


import org.maestro.plotter.common.InstantRecord;

import java.time.Instant;
import java.util.Objects;

public class CommonMemoryRecord implements Comparable<CommonMemoryRecord>, InstantRecord {
    private Instant timestamp;
    private String name;
    private long initial;
    private long max;
    private long committed;
    private long used;

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getInitial() {
        return initial;
    }

    public void setInitial(long initial) {
        this.initial = initial;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getCommitted() {
        return committed;
    }

    public void setCommitted(long committed) {
        this.committed = committed;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    @Override
    public int compareTo(CommonMemoryRecord memoryRecord) {
        return this.getTimestamp().compareTo(memoryRecord.getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonMemoryRecord that = (CommonMemoryRecord) o;
        return initial == that.initial &&
                max == that.max &&
                committed == that.committed &&
                used == that.used &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(timestamp, name, initial, max, committed, used);
    }

    @Override
    public String toString() {
        return "CommonMemoryRecord{" +
                "timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", initial=" + initial +
                ", max=" + max +
                ", committed=" + committed +
                ", used=" + used +
                '}';
    }
}
