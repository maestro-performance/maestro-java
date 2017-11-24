/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orpiske.mpt.common.writers;

import org.agrona.BitUtil;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.MessageHandler;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.RecordDescriptor;
import org.agrona.concurrent.ringbuffer.RingBufferDescriptor;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class OneToOneWorkerChannel {

    /**
     * Flyweight class that wrap a sample data.<p>
     * It can't be collected or retained, but just used to read sample data.
     */
    public static final class Sample {

        private DirectBuffer buffer;
        private int offset;

        public long timestampEpochMillis() {
            return buffer.getLong(offset);
        }

        public long value() {
            return buffer.getLong(offset + Long.BYTES);
        }

    }

    private final AtomicLong missedSamples;
    private final OneToOneRingBuffer writeBuffer;
    private final UnsafeBuffer sampleBuffer;
    private final MessageHandler onRate;
    private Consumer<Sample> currentOnSample;
    private final Sample currentSample;

    public OneToOneWorkerChannel(int capacity) {
        this.missedSamples = new AtomicLong(0);
        final int contentLength = Long.BYTES * 2;
        this.sampleBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(contentLength));
        final int requiredRingBufferCapacity =
                BitUtil.findNextPositivePowerOfTwo(
                        BitUtil.findNextPositivePowerOfTwo(capacity) *
                                (BitUtil.align(contentLength + RecordDescriptor.HEADER_LENGTH, RecordDescriptor.ALIGNMENT)))
                        + RingBufferDescriptor.TRAILER_LENGTH;
        this.writeBuffer = new OneToOneRingBuffer(new UnsafeBuffer(ByteBuffer.allocateDirect(requiredRingBufferCapacity)));
        this.onRate = this::onMessage;
        this.currentSample = new Sample();
        this.currentOnSample = null;
    }

    private void onMessage(int msgTypeId, MutableDirectBuffer buffer, int index, int length) {
        assert msgTypeId == 1;
        currentSample.buffer = buffer;
        currentSample.offset = index;
        this.currentOnSample.accept(currentSample);
    }

    /**
     * Safe to be used by just one thread
     */
    public void emitRate(long startTimestampEpochMillis, long endTimestampEpochMillis) {
        sampleBuffer.putLong(0, startTimestampEpochMillis);
        sampleBuffer.putLong(Long.BYTES, endTimestampEpochMillis);
        boolean written = this.writeBuffer.write(1, sampleBuffer, 0, sampleBuffer.capacity());
        //it could fail due to the padding too: retry just one time
        if (!written) {
            this.missedSamples.lazySet(this.missedSamples.get() + 1);
        }
    }

    /**
     * Safe to be used by just one thread
     */
    public int readRate(Consumer<Sample> onRate, int limit) {
        this.currentOnSample = onRate;
        try {
            return this.writeBuffer.read(this.onRate, limit);
        } finally {
            this.currentOnSample = null;
        }
    }

    /**
     * Safe to be called concurrently
     */
    public long missedSamples() {
        return missedSamples.get();
    }

}
