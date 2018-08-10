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

import static org.junit.Assert.assertSame;

public class TestDurationBuilderTest {
    @Test
    public void testDurationAsCount() throws Exception {
        TestDuration td = TestDurationBuilder.build("30");

        assertSame(td.getClass(), DurationCount.class);
    }

    @Test
    public void testDurationAsTimeSeconds() throws Exception {
        TestDuration td = TestDurationBuilder.build("30s");

        assertSame(td.getClass(), DurationTime.class);
    }

    @Test
    public void testDurationAsTimeMinutes() throws Exception {
        TestDuration td = TestDurationBuilder.build("30m");

        assertSame(td.getClass(), DurationTime.class);
    }

    @Test
    public void testDurationAsTimeHour() throws Exception {
        TestDuration td = TestDurationBuilder.build("1h");

        assertSame(td.getClass(), DurationTime.class);
    }

    @Test
    public void testDurationAsTimeMixed() throws Exception {
        TestDuration td = TestDurationBuilder.build("1h30m1s");

        assertSame(td.getClass(), DurationTime.class);
    }
}
