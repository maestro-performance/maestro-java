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

        assertEquals("Value for 'none3' does not match",
                null,
                urlQuery.getString("none3", null));
    }


    @Test
    public void testQueryEmpty() throws Exception {
        String url = "amqp://host/queue.name";

        URLQuery urlQuery = new URLQuery(new URI(url));

        assertFalse(urlQuery.getBoolean("none1", false));
        assertEquals("Value for 'none2' does not match", new Integer(0),
                urlQuery.getInteger("none2", 0));

        assertEquals("Value for 'none3' does not match",
                null,
                urlQuery.getString("none3", null));
    }
}
