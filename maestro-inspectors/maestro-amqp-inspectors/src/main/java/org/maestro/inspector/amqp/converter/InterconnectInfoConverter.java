package org.maestro.inspector.amqp.converter;

import java.util.*;

/**
 * A converter for collected response
 */
public class InterconnectInfoConverter {

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
        List attributeNames = (List) map.get("attributeNames");
        List<List> results = (List<List>) map.get("results");

        List<Map<String, Object>> recordList = new ArrayList<>();

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