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
import java.util.*;

/**
 * A basic JMS options parsed from url
 */
class JmsOptions {
    private static final Logger logger = LoggerFactory.getLogger(JmsOptions.class);

    private JMSProtocol protocol;
    private int configuredLimitDestinations;
    private String type;
    private String connectionUrl;
    private String path;

    private long ttl;

    private static final Set<String> maestroOptions = new HashSet<>();

    static {
        maestroOptions.add("protocol");
        maestroOptions.add("type");
        maestroOptions.add("ttl");
        maestroOptions.add("durable");
        maestroOptions.add("priority");
        maestroOptions.add("limitDestinations");
        maestroOptions.add("sessionMode");
        maestroOptions.add("batchAcknowledge");
    }

    private boolean durable;
    private int sessionMode;
    private int priority;
    private int batchAcknowledge;



    public JmsOptions(final String url) {
        try {
            final URI uri = new URI(url);
            final URLQuery urlQuery = new URLQuery(uri);

            protocol = JMSProtocol.valueOf(urlQuery.getString("protocol", JMSProtocol.AMQP.name()));

            path = uri.getPath();
            type = urlQuery.getString("type", "queue");
            configuredLimitDestinations = urlQuery.getInteger("limitDestinations", 0);

            durable = urlQuery.getBoolean("durable", false);
            priority = urlQuery.getInteger("priority", 0);
            ttl = urlQuery.getLong("ttl", 0L);
            sessionMode = urlQuery.getInteger("sessionMode", Session.AUTO_ACKNOWLEDGE);
            batchAcknowledge = urlQuery.getInteger("batchAcknowledge", 0);

            connectionUrl = filterJMSURL(uri);


        } catch (Throwable t) {
            logger.warn("Something wrong happened while parsing arguments from url : {}", t.getMessage(), t);
        }
    }

    // JMS urls cannot have the query part
    private String filterJMSURL(final URI uri) {
        URLQuery query = new URLQuery(uri);

        Map<String, String> params = query.getParams();
        StringBuilder queryStringBuilder = new StringBuilder();

        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (!maestroOptions.contains(key)) {
                queryStringBuilder.append(key);

                String hasParams = params.get(key);
                if (hasParams != null) {
                    queryStringBuilder.append("=").append(params.get(key));
                }

                if (it.hasNext()) {
                    queryStringBuilder.append("&");
                }
            }
        }
        String queryString = queryStringBuilder.length() > 0 ? "?" + queryStringBuilder : "";
        return uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + queryString;
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

    public int getBatchAcknowledge() {
        return batchAcknowledge;
    }
}
