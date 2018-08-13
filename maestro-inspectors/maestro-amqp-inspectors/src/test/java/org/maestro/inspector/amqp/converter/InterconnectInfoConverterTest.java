package org.maestro.inspector.amqp.converter;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


public class InterconnectInfoConverterTest {

    @Test
    public void parseReceivedMessage() {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();

        Map<String, Object> fakeMap = buildTestMap();

        List ret = converter.parseReceivedMessage(fakeMap);

        assertNotNull(ret);
        assertEquals(3, ret.size());

        Map<String, Object> map1 = (Map<String, Object>) ret.get(0);
        assertNotNull(map1);
        assertEquals(2, map1.size());

        assertEquals(map1.get("attribute1"), "value1.1");
        assertEquals(map1.get("attribute2"), "value1.2");

        Map<String, Object> map2 = (Map<String, Object>) ret.get(1);
        assertNotNull(map2);
        assertEquals(2, map2.size());

        assertEquals(map2.get("attribute1"), "value2.1");
        assertEquals(map2.get("attribute2"), "value2.2");

        Map<String, Object> map3 = (Map<String, Object>) ret.get(2);
        assertNotNull(map3);
        assertEquals(2, map3.size());

        assertEquals(map3.get("attribute1"), "value3.1");
        assertEquals(map3.get("attribute2"), "value3.2");
    }

    @Test
    public void parseReceivedMessageEmptyMap() {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();
        List ret = converter.parseReceivedMessage(new HashMap<String, Object>());

        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    @Test
    public void parseReceivedMessageInvalidAttributeNames() {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();
        List ret = converter.parseReceivedMessage(buildInvalidMap());

        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    @Test
    public void parseReceivedMessageWithEmptyLists() {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();
        List ret = converter.parseReceivedMessage(buildMapWithEmptyLists());

        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    private Map<String, Object> buildTestMap() {
        List<String> attributeNames = Arrays.asList("attribute1", "attribute2");

        List<String> attributesList1 = Arrays.asList("value1.1", "value1.2");
        List<String> attributesList2 = Arrays.asList("value2.1", "value2.2");
        List<String> attributesList3 = Arrays.asList("value3.1", "value3.2");

        List<List> results = Arrays.asList(attributesList1, attributesList2, attributesList3);
        Map<String, Object> fakeMap = new HashMap<>();

        fakeMap.put("attributeNames", attributeNames);
        fakeMap.put("results", results);
        return fakeMap;
    }

    private Map<String, Object> buildInvalidMap() {
        List<String> attributeNames = Arrays.asList("attribute1", "attribute2");

        List<String> attributesList1 = Arrays.asList("value1.1", "value1.2");
        List<String> attributesList2 = Arrays.asList("value2.1", "value2.2");
        List<String> attributesList3 = Arrays.asList("value3.1", "value3.2");

        List<List> results = Arrays.asList(attributesList1, attributesList2, attributesList3);
        Map<String, Object> fakeMap = new HashMap<>();

        fakeMap.put("thisIsAnInvalidAttributeKeyName", attributeNames);
        fakeMap.put("notResults", results);
        return fakeMap;
    }

    private Map<String, Object> buildMapWithEmptyLists() {
        List<String> attributeNames = new ArrayList<>(1);

        List<List> results = new ArrayList<>(1);
        Map<String, Object> fakeMap = new HashMap<>();

        fakeMap.put("attributeNames", attributeNames);
        fakeMap.put("results", results);
        return fakeMap;
    }

}
