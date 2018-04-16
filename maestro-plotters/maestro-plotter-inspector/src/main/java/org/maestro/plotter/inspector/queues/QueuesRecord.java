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

package org.maestro.plotter.inspector.queues;

import org.maestro.plotter.common.InstantRecord;

import java.time.Instant;
import java.util.Objects;

/**
 * Records for the queue information
 */
public class QueuesRecord implements Comparable<QueuesRecord>, InstantRecord {
    private Instant timestamp;
    private String name;
    private long added;
    private long count;
    private long acknowledged;
    private long expired;
    private long consumers;

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

    public long getAdded() {
        return added;
    }

    public void setAdded(long added) {
        this.added = added;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(long acknowledged) {
        this.acknowledged = acknowledged;
    }

    public long getExpired() {
        return expired;
    }

    public void setExpired(long expired) {
        this.expired = expired;
    }

    public long getConsumers() {
        return consumers;
    }

    public void setConsumers(long consumers) {
        this.consumers = consumers;
    }

    @Override
    public int compareTo(QueuesRecord queuesRecord) {
        return this.getTimestamp().compareTo(queuesRecord.getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueuesRecord that = (QueuesRecord) o;
        return added == that.added &&
                count == that.count &&
                acknowledged == that.acknowledged &&
                expired == that.expired &&
                consumers == that.consumers &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(timestamp, name, added, count, acknowledged, expired, consumers);
    }

    @Override
    public String toString() {
        return "QueuesRecord{" +
                "timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", added=" + added +
                ", count=" + count +
                ", acknowledged=" + acknowledged +
                ", expired=" + expired +
                ", consumers=" + consumers +
                '}';
    }
}

