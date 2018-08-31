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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Factory to create different implementations of {@link EpochMicroClock} depending on the precision allowed by the OS.
 */
public final class EpochClocks {

    private static final Logger LOGGER = LoggerFactory.getLogger(EpochClocks.class);
    private static final EpochMicroClock SHARED_CLOCK;
    private static final boolean SUPPORT_MICRO_CLOCKS;

    static {
        //TODO handle any failures while loading the native libs
        final String OS_NAME = System.getProperty("os.name").toLowerCase();
        if (OS_NAME.contains("linux")) {
            SUPPORT_MICRO_CLOCKS = true;
            SHARED_CLOCK = new ThreadLocalEpochMicroClock(JnaDirectNativeClock::new);
        } else {
            SUPPORT_MICRO_CLOCKS = false;
            SHARED_CLOCK = null;
            LOGGER.warn("Microseconds precision clock is not supported: will be used the millis based on in place of it");
        }
    }

    private static EpochMicroClock vanillaMillis() {
        return () -> TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
    }

    private EpochClocks() {

    }

    /**
     * It creates a fresh new {@link EpochMicroClock} instance that could be used safely by many threads.
     * @return a new {@link EpochMicroClock} instance that could be used safely by many threads.
     */
    public static EpochMicroClock sharedMicro() {
        if (SUPPORT_MICRO_CLOCKS) {
            return SHARED_CLOCK;
        } else {
            return vanillaMillis();
        }
    }

    /**
     * It creates a fresh new {@link EpochMicroClock} instance that could be used safely just by 1 thread.
     * @return a new {@link EpochMicroClock} instance that could be used safely just by 1 thread.
     */
    public static EpochMicroClock exclusiveMicro() {
        if (SUPPORT_MICRO_CLOCKS) {
            return new JnaDirectNativeClock();
        } else {
            return vanillaMillis();
        }
    }
}
