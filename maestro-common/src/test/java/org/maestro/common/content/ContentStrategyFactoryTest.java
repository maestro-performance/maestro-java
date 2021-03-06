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

public class ContentStrategyFactoryTest {

    @Test
    public void testParse() {
        assertTrue(ContentStrategyFactory.parse("100") instanceof FixedSizeContent);
        assertTrue(ContentStrategyFactory.parse("~100") instanceof VariableSizeContent);
    }
}