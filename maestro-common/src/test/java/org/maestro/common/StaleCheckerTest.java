package org.maestro.common;

import org.junit.Test;
import org.junit.Assert;

public class StaleCheckerTest {

    @Test
    public void testStale() {
        StaleChecker staleChecker = new NonProgressingStaleChecker(10);

        for (int i = 0; i < 10; i++) {
            Assert.assertFalse(staleChecker.isStale(i));
        }

        for (int i = 0; i < 9; i++) {
            Assert.assertFalse(staleChecker.isStale(10));
        }

        Assert.assertTrue(staleChecker.isStale(10));
    }
}
