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

package org.maestro.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URLUtilsTest {

    @Test
    public void testURLPathRewrite() {
        String originalUrl = "amqp://host:5672/queue";
        String expectedUrl = "amqp://host:5672/queue.worker";

        String ret = URLUtils.rewritePath(originalUrl, "worker");

        assertEquals(expectedUrl, ret);
    }

    @Test
    public void testURLPathRewriteWithUser() {
        String originalUrl = "amqp://user:password@host:5672/queue";
        String expectedUrl = "amqp://user:password@host:5672/queue.worker";

        String ret = URLUtils.rewritePath(originalUrl, "worker");

        assertEquals(expectedUrl, ret);
    }

    @Test
    public void testURLPathRewriteWithQuery() {
        String originalUrl = "amqp://host:5672/queue?limitDestinations=1";
        String expectedUrl = "amqp://host:5672/queue.worker?limitDestinations=1";

        String ret = URLUtils.rewritePath(originalUrl, "worker");

        assertEquals(expectedUrl, ret);
    }

    @Test
    public void testURLPathRewriteWithUserWithQuery() {
        String originalUrl = "amqp://user:password@host:5672/queue?limitDestinations=1";
        String expectedUrl = "amqp://user:password@host:5672/queue.worker?limitDestinations=1";

        String ret = URLUtils.rewritePath(originalUrl, "worker");

        assertEquals(expectedUrl, ret);
    }
}
