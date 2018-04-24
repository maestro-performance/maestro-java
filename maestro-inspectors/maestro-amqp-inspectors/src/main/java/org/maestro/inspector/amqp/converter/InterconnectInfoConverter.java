package org.maestro.inspector.amqp.converter;

import java.util.*;

public class InterconnectInfoConverter {

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