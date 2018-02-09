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

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;


public abstract class timespec extends Structure {
    private static final List<String> FIELDS = Arrays.asList("tv_sec", "tv_nsec");

    protected static final int CLOCK_REALTIME = 0;
    protected static final int CLOCK_MONOTONIC = 1;
    protected static final int CLOCK_PROCESS_CPUTIME_ID = 2;
    protected static final int CLOCK_THREAD_CPUTIME_ID = 3;
    protected static final int CLOCK_MONOTONIC_RAW = 4;
    protected static final int CLOCK_REALTIME_COARSE = 5;
    protected static final int CLOCK_MONOTONIC_COARSE = 6;
    protected static final int CLOCK_BOOTTIME = 7;
    protected static final int CLOCK_REALTIME_ALARM = 8;
    protected static final int CLOCK_BOOTTIME_ALARM = 9;
    protected static final int CLOCK_SGI_CYCLE = 10;     /* Hardware specific */
    protected static final int CLOCK_TAI = 11;

    /**
     * Seconds.<br>
     * C type : __time_t
     */
    public long tv_sec;
    /**
     * Nanoseconds.<br>
     * C type : __syscall_slong_t
     */
    public long tv_nsec;

    protected timespec() {
        super();
    }

    protected List<String> getFieldOrder() {
        return FIELDS;
    }

    public static final class ByReference extends timespec implements Structure.ByReference {
    }


    public static final class ByValue extends timespec implements Structure.ByValue {

    }

}
