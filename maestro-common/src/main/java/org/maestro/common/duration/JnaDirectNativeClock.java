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

package org.maestro.common.duration;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import org.agrona.BufferUtil;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.util.concurrent.TimeUnit;

public final class JnaDirectNativeClock implements EpochMicroClock {

    private static final int TIMESPEC_REQUIRED_CAPACITY;
    private final DirectBuffer buffer;
    private final Pointer pointer;

    public static native void clock_gettime(int i, Pointer ts);

    static {
        //disable bounds checks
        System.setProperty(UnsafeBuffer.DISABLE_BOUNDS_CHECKS_PROP_NAME, Boolean.TRUE.toString());
        Native.register(Platform.C_LIBRARY_NAME);
        final timespec.ByValue timeSpec = new timespec.ByValue();
        TIMESPEC_REQUIRED_CAPACITY = timeSpec.size();
    }

    public JnaDirectNativeClock() {
        buffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(TIMESPEC_REQUIRED_CAPACITY, Long.BYTES));
        this.pointer = Pointer.createConstant(buffer.addressOffset());
    }

    public long currentTimeNanos() {
        clock_gettime(timespec.CLOCK_REALTIME, pointer);
        final long tv_sec = buffer.getLong(0);
        final long tv_ns = buffer.getLong(Long.BYTES);
        return TimeUnit.SECONDS.toNanos(tv_sec) + tv_ns;
    }

    public long currentTimeMicros() {
        clock_gettime(timespec.CLOCK_REALTIME, pointer);
        final long tv_sec = buffer.getLong(0);
        final long tv_ns = buffer.getLong(Long.BYTES);
        return TimeUnit.SECONDS.toMicros(tv_sec) + (tv_ns / 1000);
    }

    @Override
    public long microTime() {
        return currentTimeMicros();
    }
}