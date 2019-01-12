package org.maestro.common;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.Assert.*;

public class LangUtilsTest {

    private class MockCloseeable implements Closeable {
        private boolean closeCalled = false;

        @Override
        public void close() throws IOException {
            closeCalled = true;
        }
    }

    @Test
    public void closeQuietly() {
        LangUtils.closeQuietly(null);

        MockCloseeable mock = new MockCloseeable();
        LangUtils.closeQuietly(mock);
        assertTrue(mock.closeCalled);
    }
}