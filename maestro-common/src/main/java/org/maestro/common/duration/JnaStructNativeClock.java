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

import java.util.concurrent.TimeUnit;

public final class JnaStructNativeClock implements EpochMicroClock {

    public static native void clock_gettime(int i, timespec ts);

    static {
        Native.register(Platform.C_LIBRARY_NAME);
    }

    private final timespec ts;

    public JnaStructNativeClock() {
        this.ts = new timespec.ByReference();
    }

    public long currentTimeNanos() {
        clock_gettime(timespec.CLOCK_REALTIME, ts);
        final long tv_sec = ts.tv_sec;
        final long tv_ns = ts.tv_nsec;
        return TimeUnit.SECONDS.toNanos(tv_sec) + tv_ns;
    }

    public long currentTimeMicros() {
        clock_gettime(timespec.CLOCK_REALTIME, ts);
        final long tv_sec = ts.tv_sec;
        final long tv_ns = ts.tv_nsec;
        return TimeUnit.SECONDS.toMicros(tv_sec) + (tv_ns / 1000);
    }

    @Override
    public long microTime() {
        return currentTimeMicros();
    }
}