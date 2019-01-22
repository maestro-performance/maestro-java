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

package org.maestro.common.duration;

import org.junit.Test;

import static org.junit.Assert.*;

public class DurationCountTest {

    public static class DurationCountTestProgress implements TestDuration.TestProgress {
        private long count = 0;

        public void increment() {
            count++;
        }

        @Override
        public long startedEpochMillis() {
            return 0;
        }

        @Override
        public long messageCount() {
            return count;
        }
    }

    @Test
    public void testCanContinue() {
        DurationCount durationCount = new DurationCount("10");

        assertEquals(10L, durationCount.getNumericDuration());

        DurationCountTestProgress progress = new DurationCountTestProgress();

        for (int i = 0; i < durationCount.getNumericDuration(); i++) {
            assertTrue(durationCount.canContinue(progress));
            progress.increment();
        }

        assertFalse(durationCount.canContinue(progress));
        progress.increment();
        assertFalse(durationCount.canContinue(progress));

        assertEquals("count", durationCount.durationTypeName());
        assertEquals("10", durationCount.toString());

        // this may not be true for 1.6, but for now let's assume it as always true
        assertEquals(durationCount.getCoolDownDuration(), durationCount.getWarmUpDuration());

    }
}