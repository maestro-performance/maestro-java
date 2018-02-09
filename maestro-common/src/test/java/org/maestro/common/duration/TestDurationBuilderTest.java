package org.maestro.common.duration;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestDurationBuilderTest {
    @Test
    public void testDurationAsCount() throws Exception {
        TestDuration td = TestDurationBuilder.build("30");

         assertTrue(td.getClass() == DurationCount.class);
    }

    @Test
    public void testDurationAsTimeSeconds() throws Exception {
        TestDuration td = TestDurationBuilder.build("30s");

        assertTrue(td.getClass() == DurationTime.class);
    }

    @Test
    public void testDurationAsTimeMinutes() throws Exception {
        TestDuration td = TestDurationBuilder.build("30m");

        assertTrue(td.getClass() == DurationTime.class);
    }

    @Test
    public void testDurationAsTimeHour() throws Exception {
        TestDuration td = TestDurationBuilder.build("1h");

        assertTrue(td.getClass() == DurationTime.class);
    }

    @Test
    public void testDurationAsTimeMixed() throws Exception {
        TestDuration td = TestDurationBuilder.build("1h30m1s");

        assertTrue(td.getClass() == DurationTime.class);
    }
}
