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
import org.maestro.common.exceptions.DurationParseException;


import static org.junit.Assert.*;

public class DurationTimeTest {
    private static class DurationTimeProgress implements TestDuration.TestProgress {
        private final long started;

        public DurationTimeProgress(long started) {
            this.started = started;
        }

        @Override
        public long startedEpochMillis() {
            return started;
        }

        @Override
        public long messageCount() {
            return 0;
        }
    }



    @Test(timeout = 15000)
    public void testCanContinue() throws DurationParseException {
        DurationTime durationTime = new DurationTime("5s");
        DurationTimeProgress progress = new DurationTimeProgress(System.currentTimeMillis());

        assertTrue(durationTime.canContinue(progress));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            fail("Interrupted while waiting for the test to timeout");
        }

        assertFalse(durationTime.canContinue(progress));

        assertEquals("time", durationTime.durationTypeName());
        assertEquals(5, durationTime.getNumericDuration());

        // this may not be true for 1.6, but for now let's assume it as always true
        assertEquals(durationTime.getCoolDownDuration(), durationTime.getWarmUpDuration());
    }

    @Test
    public void testExpectedDuration() throws DurationParseException {
        assertEquals(5, new DurationTime("5s").getNumericDuration());
        assertEquals(300, new DurationTime("5m").getNumericDuration());
        assertEquals(3900, new DurationTime("1h5m").getNumericDuration());
    }

    @Test
    public void testToStringRetainFormat() throws DurationParseException {
        assertEquals("5s", new DurationTime("5s").toString());
        assertEquals("5m", new DurationTime("5m").toString());
        assertEquals("1h5m", new DurationTime("1h5m").toString());
    }
}