package org.maestro.inspector.amqp.converter;

import org.maestro.common.inspector.types.RouterLinkInfo;

import java.util.*;

public class RouterLinkInfoConverter {

    public Map<String, List<String>> parseReceivedMessageAdam(Map map) {
        Map<String, List<String>> parsedMap = new HashMap<String, List<String>>();

        ArrayList attributeNames = (ArrayList) map.get("attributeNames");
        ArrayList<ArrayList> results = (ArrayList<ArrayList>) map.get("results");  // TODO: try .values() [Collection inf] instead

        for (Object attributeName : attributeNames) {
            ArrayList<String> tempResults = new ArrayList<>();
            for (ArrayList<String> result : results) {
                tempResults.add(result.get(attributeNames.indexOf(attributeName)));
            }

            parsedMap.put((String) attributeName, tempResults);
        }
        return parsedMap;
    }

    public RouterLinkInfo parseReceivedMessage(Map map) {
        List<Map<String, Object>> recordList = new ArrayList<>();

        List attributeNames = (List) map.get("attributeNames");
        List<List> results = (List<List>) map.get("results");  // TODO: try .values() [Collection inf] instead


        for (List result : results) {
            Map<String, Object> tmpRecord = new HashMap<>();
            for (Object attributeName: attributeNames) {
                tmpRecord.put((String) attributeName, result.get(attributeNames.indexOf(attributeName)));
            }
            recordList.add(Collections.unmodifiableMap(tmpRecord));
            //recordList.add(tmpRecord);
        }
        
        return new RouterLinkInfo(recordList);
    }
}