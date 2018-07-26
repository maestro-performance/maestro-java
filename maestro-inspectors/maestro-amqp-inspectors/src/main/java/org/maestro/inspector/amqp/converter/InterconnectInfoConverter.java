package org.maestro.inspector.amqp.converter;

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
    @SuppressWarnings("unchecked")
    public List parseReceivedMessage(Map map) {
        List<Map<String, Object>> recordList = new ArrayList<>();

        if (map == null || map.isEmpty()) {
            logger.warn("The received attribute map is empty or null");
            return recordList;
        }

        List attributeNames = (List) map.get("attributeNames");
        if (attributeNames == null) {
            logger.warn("The received attribute map does not contain a list of attribute names");
            return recordList;
        }

        List<List> results = (List<List>) map.get("results");
        if (results == null) {
            logger.warn("The received attribute map does not contain a list of attribute values (results)");
            return recordList;
        }

        for (List result : results) {
            Map<String, Object> tmpRecord = new HashMap<>();
            for (Object attributeName: attributeNames) {
                tmpRecord.put((String) attributeName, result.get(attributeNames.indexOf(attributeName)));
            }
            recordList.add(Collections.unmodifiableMap(tmpRecord));
        }
        
        return recordList;
    }
}