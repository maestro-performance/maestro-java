/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.inspector.amqp.converter;

import org.maestro.common.exceptions.MaestroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A converter for collected response
 */
public class InterconnectInfoConverter {
    private static final Logger logger = LoggerFactory.getLogger(InterconnectInfoConverter.class);

    /**
     * Parse received message body, in a Map format, into a list collection.
     * Basically it converts a map object containing 2 entries (one for attributes
     * and another for the attribute values (results)) into a list of maps of
     * attributes and values. Check InterconnectInfoConverterTest for details.
     *
     * @param map collected data
     * @return parsed into into list of hash maps
     */
    public List<Map<String, Object>> parseReceivedMessage(Map<?, ?> map) {
        List<Map<String, Object>> recordList = new ArrayList<>();

        if (map == null || map.isEmpty()) {
            logger.warn("The received attribute map is empty or null");
            return recordList;
        }

        Object tmpAttributeNames = map.get("attributeNames");
        if (tmpAttributeNames != null && !(tmpAttributeNames instanceof List)) {
            throw new MaestroException("Unexpected type for the returned attribute names: ");
        }


        List<?> attributeNames = (List) tmpAttributeNames;
        if (attributeNames == null) {
            logger.warn("The received attribute map does not contain a list of attribute names");
            return recordList;
        }

        Object tmpResults = map.get("results");
        if (tmpResults != null && !(tmpResults instanceof List)) {
            throw new MaestroException("Unexpected type for the returned attribute values");
        }

        List<List> results = (List<List>) tmpResults;
        if (results == null) {
            logger.warn("The received attribute map does not contain a list of attribute values (results)");
            return recordList;
        }

        for (List<?> result : results) {
            Map<String, Object> tmpRecord = new HashMap<>();
            for (Object attributeName: attributeNames) {
                tmpRecord.put((String) attributeName, result.get(attributeNames.indexOf(attributeName)));
            }
            recordList.add(Collections.unmodifiableMap(tmpRecord));
        }
        
        return recordList;
    }
}