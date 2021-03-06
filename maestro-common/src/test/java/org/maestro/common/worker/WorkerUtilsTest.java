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

package org.maestro.common.worker;

import org.junit.Test;

import static org.junit.Assert.*;

public class WorkerUtilsTest {
    @Test
    public void testRate() {
        assertEquals(0, WorkerUtils.getExchangeInterval(0));

        // For a rate of 50k msg/sec, send the messages at every 20000 nanoseconds (0.02 milliseconds)
        assertEquals(20000, WorkerUtils.getExchangeInterval(50000));
    }
}