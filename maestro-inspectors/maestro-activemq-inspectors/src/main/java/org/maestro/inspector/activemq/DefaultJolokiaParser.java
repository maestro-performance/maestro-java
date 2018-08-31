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

package org.maestro.inspector.activemq;

import org.json.simple.JSONObject;
import org.maestro.inspector.activemq.converter.MapConverter;
import org.maestro.inspector.activemq.converter.QueueInfoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse the Jolokia properties and convert them to inspector types
 */
public class DefaultJolokiaParser {
    private static final Logger logger = LoggerFactory.getLogger(DefaultJolokiaParser.class);

    public DefaultJolokiaParser() {
    }

    /**
     * Parse a jolokia property
     * @param converter the jolokia converter to use
     * @param key
     * @param object
     */
    void parse(final JolokiaConverter converter, final Object key, final Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace("Processing returned JSON Key {} with value: {}", key, object);
        }

        String jolokiaPropertyName = "";
        if (key instanceof String) {
            String tmp = (String) key;

            logger.trace("Checking property name/group {}", tmp);

            Pattern pattern = Pattern.compile(".*name=(.*),.*");
            Matcher matcher = pattern.matcher(tmp);

            if (matcher.matches()) {
                jolokiaPropertyName = matcher.group(1);

                logger.trace("Reading property name/group '{}'", jolokiaPropertyName);
            } else {
                jolokiaPropertyName = tmp;
            }
        }

        converter.convert(jolokiaPropertyName, object);
    }

    /**
     * Parse a jolokia property
     * @param converter
     * @param key
     * @param object
     */
    void parseQueueInfo(final QueueInfoConverter converter, final Object key, final Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace("Processing returned JSON Key {} with value: {}", key, object);
        }

        JSONObject jo;
        if (object instanceof JSONObject) {
            jo = (JSONObject) object;
        }
        else {
            return;
        }

        Map<String, Object> queueProperties = new HashMap<>();
        JolokiaConverter innerConverters = new MapConverter(queueProperties);
        jo.forEach((param, value) -> parse(innerConverters, param, value));


        Object tmpName = queueProperties.get("Name");

        if (tmpName instanceof String) {
            String queueName = (String) tmpName;
            converter.convert(queueName, queueProperties);
        }
        else {
            String jolokiaPropertyName = "";
            if (key instanceof String) {
                String tmp = (String) key;
                logger.trace("Checking property name/group {}", tmp);

                Pattern pattern = Pattern.compile(".*address=(\".*\"),.*");
                Matcher matcher = pattern.matcher(tmp);

                if (matcher.matches()) {
                    jolokiaPropertyName = matcher.group(1);

                    logger.trace("Reading property name/group '{}'", jolokiaPropertyName);
                } else {
                    jolokiaPropertyName = tmp;
                }
            }

            converter.convert(jolokiaPropertyName, queueProperties);
        }
    }
}