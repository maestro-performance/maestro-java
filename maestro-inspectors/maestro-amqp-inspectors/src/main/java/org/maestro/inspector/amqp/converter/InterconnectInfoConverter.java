package org.maestro.inspector.amqp.converter;

import java.util.*;

/**
 * A converter for collected response
 */
public class InterconnectInfoConverter {

    /**
     * Parse received message body into better collection
     * @param map collected data
     * @return parsed into into list of hash maps
     */
    @SuppressWarnings("unchecked")
    public List parseReceivedMessage(Map map) {
        List<Map<String, Object>> recordList = new ArrayList<>();

        List attributeNames = (List) map.get("attributeNames");
        List<List> results = (List<List>) map.get("results");


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