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
import java.util.Map;

import static org.junit.Assert.*;

public class URLQueryTest {
    @Test
    public void testQuery() throws Exception {
        String url = "amqp://host/queue.name?durable=true&somethingElse=123&otherSomething=abc";

        URLQuery urlQuery = new URLQuery(new URI(url));

        assertTrue(urlQuery.getBoolean("durable", false));
        assertEquals("Value for 'somethingElse' does not match", Integer.valueOf(123),
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
        assertEquals("Value for 'none2' does not match", Integer.valueOf(0),
                urlQuery.getInteger("none2", 0));

        assertNull("Value for 'none3' does not match", urlQuery.getString("none3", null));
    }


    @Test
    public void testQueryEmpty() throws Exception {
        String url = "amqp://host/queue.name";

        URLQuery urlQuery = new URLQuery(url);

        assertFalse(urlQuery.getBoolean("none1", false));
        assertEquals("Value for 'none2' does not match", Integer.valueOf(0),
                urlQuery.getInteger("none2", 0));

        assertNull("Value for 'none3' does not match", urlQuery.getString("none3", null));
    }

    @Test
    public void testQueryWithFalse() throws Exception {
        String url = "amqp://host/queue.name?value1=false&value2=true";

        URLQuery urlQuery = new URLQuery(new URI(url));

        assertFalse(urlQuery.getBoolean("value1", true));
        assertTrue(urlQuery.getBoolean("value2", false));
    }

    @Test
    public void testQueryWithLong() throws Exception {
        String url = "amqp://host/queue.name?value1=1234";

        URLQuery urlQuery = new URLQuery(new URI(url));

        assertEquals(1234L, (long) urlQuery.getLong("value1", 0L));
    }

    @Test
    public void testQueryWitMaps() throws Exception {
        String url = "amqp://host/queue.name?value1=1234&value2=true";

        URLQuery urlQuery = new URLQuery(new URI(url));

        Map<String, String> map = urlQuery.getParams();

        assertTrue(map.size() == 2);
        assertTrue(urlQuery.count() == 2);
    }
}
