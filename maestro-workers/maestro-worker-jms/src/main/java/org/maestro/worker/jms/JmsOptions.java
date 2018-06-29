/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
  NOTE: this a fork of Justin Ross' Quiver at:
  https://raw.githubusercontent.com/ssorj/quiver/master/java/quiver-jms-driver/src/main/java/net/ssorj/quiver/QuiverArrowJms.java
  <p>
  The code was modified to integrate more tightly with maestro.
 */

package org.maestro.worker.jms;

import org.maestro.common.URLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;
import java.net.URI;

/**
 * A basic JMS options parsed from url
 */
class JmsOptions {
    private static final Logger logger = LoggerFactory.getLogger(JmsOptions.class);

    private String url;
    private JMSProtocol protocol;
    private int configuredLimitDestinations;
    private String type;
    private String connectionUrl;
    private String path;

    private long ttl;
    private boolean durable;
    private int sessionMode;
    private int priority;


    public JmsOptions(String url) {
        try {
            final URI uri = new URI(url);
            final URLQuery urlQuery = new URLQuery(uri);

            connectionUrl = filterURL().replace(path, "");
            protocol = JMSProtocol.valueOf(urlQuery.getString("protocol", JMSProtocol.AMQP.name()));
            path = uri.getPath();
            type = urlQuery.getString("type", "queue");
            configuredLimitDestinations = urlQuery.getInteger("limitDestinations", null);

            durable = urlQuery.getBoolean("durable", false);
            priority = urlQuery.getInteger("priority", null);
            ttl = urlQuery.getLong("ttl", null);
            sessionMode = urlQuery.getInteger("sessionMode", Session.AUTO_ACKNOWLEDGE);


        } catch (Throwable t) {
            logger.warn("Something wrong happened while parsing arguments from url : {}", t.getMessage(), t);
        }
    }

    // JMS urls cannot have the query part
    private String filterURL() {
        String filteredUrl;

        int queryStartIndex = url.indexOf('?');
        if (queryStartIndex != -1) {
            filteredUrl = url.substring(0, queryStartIndex);
        } else {
            filteredUrl = url;
        }

        return filteredUrl;
    }

    public String getUrl() {
        return url;
    }

    public JMSProtocol getProtocol() {
        return protocol;
    }

    public long getTtl() {
        return ttl;
    }

    public boolean isDurable() {
        return durable;
    }

    public int getSessionMode() {
        return sessionMode;
    }

    public int getConfiguredLimitDestinations() {
        return configuredLimitDestinations;
    }

    public String getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getPath() {
        return path;
    }
}
