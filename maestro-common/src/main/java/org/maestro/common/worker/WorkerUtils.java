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

package org.maestro.common.worker;

import java.util.concurrent.locks.LockSupport;

/**
 * Worker utilities
 */
public class WorkerUtils {
    private WorkerUtils() {}

    /**
     * Gets the interval used for exchanging messages (ie.: sending them at a regular pace)
     * @param rate The given rate
     * @return the interval in nanoseconds or 0 if the rate is unbounded
     */
    public static long getExchangeInterval(final long rate) {
        return rate > 0 ? (1_000_000_000L / rate) : 0;
    }


    public static long waitNanoInterval(final long expectedFireTime, final long intervalInNanos) {
        assert intervalInNanos > 0;
        long now;
        do {
            now = System.nanoTime();
            if ((now - expectedFireTime) < 0) {
                LockSupport.parkNanos(expectedFireTime - now);
            }
        } while ((now - expectedFireTime) < 0);

        return now;
    }
}
