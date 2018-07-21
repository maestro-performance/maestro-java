package org.maestro.test.utils;

import org.junit.Test;
import org.maestro.tests.utils.SyncUtils;

import static org.junit.Assert.*;

public class SyncUtilsTest {

    @Test
    public void testFlushWaitResetCheckSmallPcSmallSize() {
        assertFalse(SyncUtils.flushWaitResetCheck(10, 10));
    }

    @Test
    public void testFlushWaitResetCheckSmallPcLargeSize() {
        assertTrue(SyncUtils.flushWaitResetCheck(10, 10241));
    }

    @Test
    public void testFlushWaitResetCheckLargePcSmallSize() {
        assertTrue(SyncUtils.flushWaitResetCheck(200, 10));
    }

    @Test
    public void testFlushWaitResetCheckLargePcLargeSize() {
        assertTrue(SyncUtils.flushWaitResetCheck(100, 10241));
    }

    @Test
    public void testFlushWaitResetCheckSoftLimitsNotInclusive() {
        assertFalse(SyncUtils.flushWaitResetCheck(30, 2048));
    }

    @Test
    public void testFlushWaitResetCheckSoftLimits() {
        assertTrue(SyncUtils.flushWaitResetCheck(31, 2048));
    }
}
