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

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class FixedSizeContentTest {

    @Test
    public void testPepareContent() {
        FixedSizeContent content = new FixedSizeContent(100);

        for (int i = 0; i < 100; i++) {
            ByteBuffer buffer = content.prepareContent();

            final int length = buffer.remaining();

            assertEquals(100, length);
        }
    }
}