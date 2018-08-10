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

import java.net.URI;

import static org.junit.Assert.*;

public class URLQueryTest {
    @Test
    public void testQuery() throws Exception {
        String url = "amqp://host/queue.name?durable=true&somethingElse=123&otherSomething=abc";

        URLQuery urlQuery = new URLQuery(new URI(url));

        assertTrue(urlQuery.getBoolean("durable", false));
        assertEquals("Value for 'somethingElse' does not match", new Integer(123),
                urlQuery.getInteger("somethingElse", 123));
        assertEquals("Value for 'otherSomething' does not match",
                "abc",
                urlQuery.getString("otherSomething", "abc"));
    }

    @Test
    public void testQueryWithNulls() throws Exception {
        String url = "amqp://host/queue.name?durable=true&somethingElse=123&otherSomething=abc";

        URLQuery urlQuery = new URLQuery(new URI(url));

        assertFalse(urlQuery.getBoolean("none1", false));
        assertEquals("Value for 'none2' does not match", new Integer(0),
                urlQuery.getInteger("none2", 0));

        assertNull("Value for 'none3' does not match", urlQuery.getString("none3", null));
    }


    @Test
    public void testQueryEmpty() throws Exception {
        String url = "amqp://host/queue.name";

        URLQuery urlQuery = new URLQuery(new URI(url));

        assertFalse(urlQuery.getBoolean("none1", false));
        assertEquals("Value for 'none2' does not match", new Integer(0),
                urlQuery.getInteger("none2", 0));

        assertNull("Value for 'none3' does not match", urlQuery.getString("none3", null));
    }
}
