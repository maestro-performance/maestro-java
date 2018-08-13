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
package org.maestro.plotter.amqp.inspector.memory;

import org.maestro.plotter.common.InstantRecord;

import java.time.Instant;
import java.util.Objects;

/**
 * A class represents single record from csv file
 */
public class QDMemoryRecord implements Comparable<QDMemoryRecord>, InstantRecord {
    private Instant timestamp;
    private String name;
    private long typeSize;
    private long totalAllocFromHeap;
    private long batchesRebalancedToThreads;
    private long totalFreeToHeap;
    private long localFreeListMax;
    private long heldByThreads;
    private long transferBatchSize;
    private long batchesRebalancedToGlobal;
    private long globalFreeListMax;

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

    public long getTypeSize() {
        return typeSize;
    }

    public void setTypeSize(long typeSize) {
        this.typeSize = typeSize;
    }

    public long getTotalAllocFromHeap() {
        return totalAllocFromHeap;
    }

    public void setTotalAllocFromHeap(long totalAllocFromHeap) {
        this.totalAllocFromHeap = totalAllocFromHeap;
    }

    public long getBatchesRebalancedToThreads() {
        return batchesRebalancedToThreads;
    }

    public void setBatchesRebalancedToThreads(long batchesRebalancedToThreads) {
        this.batchesRebalancedToThreads = batchesRebalancedToThreads;
    }

    public long getTotalFreeToHeap() {
        return totalFreeToHeap;
    }

    public void setTotalFreeToHeap(long totalFreeToHeap) {
        this.totalFreeToHeap = totalFreeToHeap;
    }

    public long getLocalFreeListMax() {
        return localFreeListMax;
    }

    public void setLocalFreeListMax(long localFreeListMax) {
        this.localFreeListMax = localFreeListMax;
    }

    public long getHeldByThreads() {
        return heldByThreads;
    }

    public void setHeldByThreads(long heldByThreads) {
        this.heldByThreads = heldByThreads;
    }

    public long getTransferBatchSize() {
        return transferBatchSize;
    }

    public void setTransferBatchSize(long transferBatchSize) {
        this.transferBatchSize = transferBatchSize;
    }

    public long getBatchesRebalancedToGlobal() {
        return batchesRebalancedToGlobal;
    }

    public void setBatchesRebalancedToGlobal(long batchesRebalancedToGlobal) {
        this.batchesRebalancedToGlobal = batchesRebalancedToGlobal;
    }

    public long getGlobalFreeListMax() {
        return globalFreeListMax;
    }

    public void setGlobalFreeListMax(long globalFreeListMax) {
        this.globalFreeListMax = globalFreeListMax;
    }

    @Override
    public int compareTo(QDMemoryRecord routerLinkRecord) {
        return this.getTimestamp().compareTo(routerLinkRecord.getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QDMemoryRecord that = (QDMemoryRecord) o;
        return batchesRebalancedToGlobal == that.batchesRebalancedToGlobal &&
                batchesRebalancedToThreads == that.batchesRebalancedToThreads &&
                globalFreeListMax == that.globalFreeListMax &&
                heldByThreads == that.heldByThreads &&
                localFreeListMax == that.localFreeListMax &&
                totalAllocFromHeap == that.totalAllocFromHeap&&
                totalFreeToHeap == that.totalFreeToHeap&&
                transferBatchSize == that.transferBatchSize&&
                typeSize == that.typeSize&&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, name, batchesRebalancedToGlobal, batchesRebalancedToThreads, globalFreeListMax,
                heldByThreads, localFreeListMax, totalAllocFromHeap, totalFreeToHeap, transferBatchSize, typeSize);
    }

    @Override
    public String toString() {
        return "RouterLinkRecord{" +
                "timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", typeSize=" + typeSize +
                ", transferBatchSize=" + transferBatchSize +
                ", totalFreeToHeap=" + totalFreeToHeap +
                ", totalAllocFromHeap=" + totalAllocFromHeap +
                ", localFreeListMax=" + localFreeListMax +
                ", heldByThreads=" + heldByThreads +
                ", globalFreeListMax=" + globalFreeListMax +
                ", batchesRebalancedToThreads=" + batchesRebalancedToThreads +
                ", batchesRebalancedToGlobal=" + batchesRebalancedToGlobal +
                '}';
    }
}
