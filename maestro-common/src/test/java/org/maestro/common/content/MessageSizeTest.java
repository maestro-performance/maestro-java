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

package org.maestro.common.content;

import org.junit.Test;

import static org.junit.Assert.*;

public class MessageSizeTest {

    @Test
    public void testVariable() {
        assertEquals("~1024", MessageSize.variable(1024));
    }

    @Test
    public void testFixed() {
        assertEquals("1024", MessageSize.fixed(1024));
    }

    @Test
    public void isVariable() {
        assertTrue(MessageSize.isVariable("~1024"));
        assertFalse(MessageSize.isVariable("1024"));
    }

    @Test
    public void toSizeFromSpec() {
        assertEquals(1024, MessageSize.toSizeFromSpec("~1024"));
    }
}