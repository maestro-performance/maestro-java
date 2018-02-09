/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.maestro.common.writers;

import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.AtomicBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.broadcast.BroadcastBufferDescriptor;
import org.agrona.concurrent.broadcast.BroadcastReceiver;
import org.agrona.concurrent.broadcast.BroadcastTransmitter;
import org.agrona.concurrent.ringbuffer.RecordDescriptor;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public final class OneToOneWorkerChannel {

    /**
     * Flyweight class that wrap a sample data.<p>
     * It can't be collected or retained, but just used to read sample data.
     */
    public static final class Sample {

        private DirectBuffer buffer;
        private int offset;

        public long timestampEpochMicros() {
            return buffer.getLong(offset);
        }

        public long value() {
            return buffer.getLong(offset + Long.BYTES);
        }

    }

    private final BroadcastTransmitter writeBuffer;
    private final BroadcastReceiver receiver;
    private final UnsafeBuffer sampleBuffer;
    private final Sample currentSample;
    private final int footprintInBytes;

    public OneToOneWorkerChannel(int capacity) {
        //agrona doesn't allow too small ring buffers
        capacity = Math.max(8, capacity);
        final int contentLength = Long.BYTES * 2;
        this.sampleBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(contentLength));
        final int requiredRingBufferCapacity =
                BitUtil.findNextPositivePowerOfTwo(
                        BitUtil.findNextPositivePowerOfTwo(capacity) *
                                (BitUtil.align(contentLength + RecordDescriptor.HEADER_LENGTH, RecordDescriptor.ALIGNMENT)))
                        + BroadcastBufferDescriptor.TRAILER_LENGTH;
        final AtomicBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(requiredRingBufferCapacity));
        this.writeBuffer = new BroadcastTransmitter(buffer);
        this.currentSample = new Sample();
        this.receiver = new BroadcastReceiver(buffer);
        this.footprintInBytes = buffer.capacity();
        this.currentSample.buffer = this.sampleBuffer;
        this.currentSample.offset = 0;
    }

    /**
     * Safe to be used by just one thread
     */
    public void emitRate(long startTimestampEpochMicros, long endTimestampEpochMicros) {
        assert startTimestampEpochMicros - endTimestampEpochMicros <= 0 : "startTimestampEpochMicros <= endTimestampEpochMicros";
        sampleBuffer.putLong(0, startTimestampEpochMicros);
        sampleBuffer.putLong(Long.BYTES, endTimestampEpochMicros);
        this.writeBuffer.transmit(1, sampleBuffer, 0, sampleBuffer.capacity());
    }

    public int footprintInBytes() {
        return this.footprintInBytes;
    }

    /**
     * Safe to be used by just one thread
     */
    public int readRate(Consumer<Sample> onRate, int limit) {
        for (int i = 0; i < limit; i++) {
            boolean valid;
            do {
                final boolean receiveNext = this.receiver.receiveNext();
                if (!receiveNext) {
                    return i;
                }
                this.sampleBuffer.putBytes(0, this.receiver.buffer(), this.receiver.offset(), this.receiver.length());
                valid = this.receiver.validate();
            } while (!valid);
            onRate.accept(currentSample);
        }
        return limit;
    }

    /**
     * Safe to be called concurrently
     */
    public long missedSamples() {
        return receiver.lappedCount();
    }

}
