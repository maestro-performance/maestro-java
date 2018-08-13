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

package org.maestro.worker.jms;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JMSOptionsTest {

    @Test
    public void testUrlWithoutProtocol() {
        final String url = "amqp://hostname:5672/test.performance.queue?durable=false&limitDestinations=5&batchAcknowledge=100";

        JmsOptions jmsOptions = new JmsOptions(url);

        assertEquals("The URL does not match the expected protocol", "AMQP", jmsOptions.getProtocol().name());
        assertFalse("The URL does not match the expected protocol", jmsOptions.isDurable());
        assertEquals("The limit destination does not match the expected value", 5,
                jmsOptions.getConfiguredLimitDestinations());

        assertEquals("The default TTL does not match the expected value", 0,
                jmsOptions.getTtl());
        assertEquals("The default priority does not match the expected value", 0,
                jmsOptions.getPriority());
        assertEquals("The backAcknowledge does not match the expected value", 100, jmsOptions.getBatchAcknowledge());

        assertEquals("The connection URL does not match the expected one",
                "amqp://hostname:5672",
                jmsOptions.getConnectionUrl());
    }

    @Test
    public void testUrlEmpty() {
        final String url = "amqp://hostname:5672/test.performance.queue";

        JmsOptions jmsOptions = new JmsOptions(url);

        assertEquals("The URL does not match the expected protocol", "AMQP", jmsOptions.getProtocol().name());
        assertFalse("The URL does not match the expected protocol", jmsOptions.isDurable());
        assertEquals("The limit destination does not match the expected value", 0,
                jmsOptions.getConfiguredLimitDestinations());

        assertEquals("The default TTL does not match the expected value", 0,
                jmsOptions.getTtl());
        assertEquals("The default priority does not match the expected value", 0,
                jmsOptions.getPriority());

        assertEquals("The connection URL does not match the expected one",
                "amqp://hostname:5672",
                jmsOptions.getConnectionUrl());
    }


    @Test
    public void testUrlWithProtocol() {
        final String url = "amqp://hostname:5672/test.performance.queue?durable=false&limitDestinations=5&protocol=OPENWIRE";

        JmsOptions jmsOptions = new JmsOptions(url);

        assertEquals("The URL does not match the expected protocol", "OPENWIRE", jmsOptions.getProtocol().name());
        assertFalse("The URL does not match the expected protocol", jmsOptions.isDurable());
        assertEquals("The limit destination does not match the expected value", 5,
                jmsOptions.getConfiguredLimitDestinations());

        assertEquals("The default TTL does not match the expected value", 0,
                jmsOptions.getTtl());
        assertEquals("The default priority does not match the expected value", 0,
                jmsOptions.getPriority());

        assertEquals("The connection URL does not match the expected one",
                "amqp://hostname:5672",
                jmsOptions.getConnectionUrl());
    }

    @Test
    public void testTlsAndJmsOptions() {
        final String url = "amqps://hostname:5671/test.performance.queue?durable=false&jms.username=user1&jms.password=pass1&transport.trustAll";
        JmsOptions jmsOptions = new JmsOptions(url);

        assertFalse("Expected durable to be false", jmsOptions.isDurable());
        assertEquals("The connection URL does not match the expected one",
                "amqps://hostname:5671?jms.username=user1&jms.password=pass1&transport.trustAll",
                jmsOptions.getConnectionUrl());
    }

    @Test
    public void testJmsOptions() {
        final String url = "amqps://hostname:5671/test.performance.queue?durable=false&jms.username=user1&jms.password=pass1";
        JmsOptions jmsOptions = new JmsOptions(url);

        assertFalse("Expected durable to be false", jmsOptions.isDurable());
        assertEquals("The connection URL does not match the expected one",
                "amqps://hostname:5671?jms.username=user1&jms.password=pass1",
                jmsOptions.getConnectionUrl());
    }
}
