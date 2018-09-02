/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.plotter.amqp.inspector.routerlink;

import org.maestro.plotter.common.InstantRecord;

import java.time.Instant;
import java.util.Objects;

/**
 * A class represents single record from csv file
 */
public class RouterLinkRecord implements Comparable<RouterLinkRecord>, InstantRecord {
    private Instant timestamp;
    private String name;
    private long capacity;
    private long deliveryCount;
    private long undeliveredCount;
    private long presettledCount;
    private long unsettledCount;
    private long droppedPresettledCount;
    private long releasedCount;
    private long modifiedCount;
    private long acceptedCount;
    private long rejectedCount;

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

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getDeliveryCount() {
        return deliveryCount;
    }

    public void setDeliveryCount(long deliveryCount) {
        this.deliveryCount = deliveryCount;
    }

    public long getUndeliveredCount() {
        return undeliveredCount;
    }

    public void setUndeliveredCount(long undeliveredCount) {
        this.undeliveredCount = undeliveredCount;
    }

    public long getPresettledCount() {
        return presettledCount;
    }

    public void setPresettledCount(long presettledCount) {
        this.presettledCount = presettledCount;
    }

    public long getUnsettledCount() {
        return unsettledCount;
    }

    public void setUnsettledCount(long unsettledCount) {
        this.unsettledCount = unsettledCount;
    }

    public long getDroppedPresettledCount() {
        return droppedPresettledCount;
    }

    public void setDroppedPresettledCount(long droppedPresettledCount) {
        this.droppedPresettledCount = droppedPresettledCount;
    }

    public long getReleasedCount() {
        return releasedCount;
    }

    public void setReleasedCount(long releasedCount) {
        this.releasedCount = releasedCount;
    }

    public long getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(long modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    public long getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(long acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    @Override
    public int compareTo(RouterLinkRecord routerLinkRecord) {
        return this.getTimestamp().compareTo(routerLinkRecord.getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouterLinkRecord that = (RouterLinkRecord) o;
        return capacity == that.capacity &&
                deliveryCount == that.deliveryCount &&
                undeliveredCount == that.undeliveredCount &&
                presettledCount == that.presettledCount &&
                unsettledCount == that.unsettledCount &&
                droppedPresettledCount == that.droppedPresettledCount &&
                releasedCount == that.releasedCount &&
                modifiedCount == that.modifiedCount &&
                acceptedCount == that.acceptedCount &&
                rejectedCount == that.rejectedCount &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, name, capacity, deliveryCount, undeliveredCount, presettledCount,
                unsettledCount, droppedPresettledCount, releasedCount, modifiedCount, acceptedCount, rejectedCount);
    }

    @Override
    public String toString() {
        return "RouterLinkRecord{" +
                "timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", deliveryCount=" + deliveryCount +
                ", undeliveredCount=" + undeliveredCount +
                ", presettledCount=" + presettledCount +
                ", unsettledCount=" + unsettledCount +
                ", droppedPresettledCount=" + droppedPresettledCount +
                ", releasedCount=" + releasedCount +
                ", modifiedCount=" + modifiedCount +
                ", acceptedCount=" + acceptedCount +
                ", rejectedCount=" + rejectedCount +
                '}';
    }
}
