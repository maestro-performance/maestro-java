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

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for parsing test target URLs. It makes it easier to parse implementation
 * specific parameters (such as 'parameter1' and 'parameter2' in
 * "amqp://host/queue{@literal ?}parameter1=true{@literal &}parameter2=value".
 */
public class URLQuery {
    private static final Logger logger = LoggerFactory.getLogger(URLQuery.class);
    private final List<NameValuePair> params;

    /**
     * Constructor
     * @param uri a URI string
     * @throws URISyntaxException if the URL is invalid
     */
    public URLQuery(final String uri) throws URISyntaxException {
        this(new URI(uri));
    }

    /**
     * Constructor
     * @param uri a URI object
     */
    public URLQuery(URI uri) {
         params = URLEncodedUtils.parse(uri, "UTF-8");

        for (NameValuePair param : params) {
            logger.trace("{}: {}", param.getName(), param.getValue());
        }
    }


    /**
     * Get a parameter value as a string
     * @param name the parameter name
     * @param defaultValue the default value if not given
     * @return the parameter value or defaultValue if not given
     */
    public String getString(final String name, final String defaultValue) {
        for (NameValuePair param : params) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }

        return defaultValue;
    }


    /**
     * Get a parameter value as a boolean
     * @param name the parameter name
     * @param defaultValue the default value if not given
     * @return the parameter value or defaultValue if not given
     */
    public boolean getBoolean(final String name, boolean defaultValue) {
        String value = getString(name, null);

        if (value == null) {
            return defaultValue;
        }

        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        else {
            if (value.equalsIgnoreCase("false")) {
                return false;
            }
        }

        return defaultValue;
    }


    /**
     * Get a parameter value as an Integer
     * @param name the parameter name
     * @param defaultValue the default value if not given
     * @return the parameter value or defaultValue if not given
     */
    public Integer getInteger(final String name, final Integer defaultValue) {
        String value = getString(name, null);

        if (value == null) {
            return defaultValue;
        }

        return Integer.parseInt(value);
    }

    /**
     * Get a parameter value as a Long
     * @param name the parameter name
     * @param defaultValue the default value if not given
     * @return the parameter value or defaultValue if not given
     */
    public Long getLong(final String name, final Long defaultValue) {
        String value = getString(name, null);

        if (value == null) {
            return defaultValue;
        }

        return Long.parseLong(value);
    }


    /**
     * Get a map of all the params in the URL
     * @return a map with all the params in the URI
     */
    @SuppressWarnings("unused")
    public Map<String, String> getParams() {
        Map<String, String> ret = new HashMap<>();

        for (NameValuePair pair : params) {
            ret.put(pair.getName(), pair.getValue());
        }

        return ret;
    }
}
