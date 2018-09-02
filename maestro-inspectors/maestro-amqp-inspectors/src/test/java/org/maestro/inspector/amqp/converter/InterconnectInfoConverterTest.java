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

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


public class InterconnectInfoConverterTest {

    @SuppressWarnings("unchecked")
    @Test
    public void parseReceivedMessage() {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();

        Map<String, Object> fakeMap = buildTestMap();

        List<Map<String, Object>> ret = converter.parseReceivedMessage(fakeMap);

        assertNotNull(ret);
        assertEquals(3, ret.size());

        Map<String, Object> map1 = ret.get(0);
        assertNotNull(map1);
        assertEquals(2, map1.size());

        assertEquals(map1.get("attribute1"), "value1.1");
        assertEquals(map1.get("attribute2"), "value1.2");

        Map<String, Object> map2 = ret.get(1);
        assertNotNull(map2);
        assertEquals(2, map2.size());

        assertEquals(map2.get("attribute1"), "value2.1");
        assertEquals(map2.get("attribute2"), "value2.2");

        Map<String, Object> map3 = ret.get(2);
        assertNotNull(map3);
        assertEquals(2, map3.size());

        assertEquals(map3.get("attribute1"), "value3.1");
        assertEquals(map3.get("attribute2"), "value3.2");
    }

    @Test
    public void parseReceivedMessageEmptyMap() {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();
        List<Map<String, Object>> ret = converter.parseReceivedMessage(new HashMap<String, Object>());

        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    @Test
    public void parseReceivedMessageInvalidAttributeNames() {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();
        List<Map<String, Object>> ret = converter.parseReceivedMessage(buildInvalidMap());

        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    @Test
    public void parseReceivedMessageWithEmptyLists() {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();
        List<Map<String, Object>> ret = converter.parseReceivedMessage(buildMapWithEmptyLists());

        assertNotNull(ret);
        assertEquals(0, ret.size());
    }

    private Map<String, Object> buildTestMap() {
        List<String> attributeNames = Arrays.asList("attribute1", "attribute2");

        List<String> attributesList1 = Arrays.asList("value1.1", "value1.2");
        List<String> attributesList2 = Arrays.asList("value2.1", "value2.2");
        List<String> attributesList3 = Arrays.asList("value3.1", "value3.2");

        List<List<String>> results = Arrays.asList(attributesList1, attributesList2, attributesList3);
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

        List<List<String>> results = Arrays.asList(attributesList1, attributesList2, attributesList3);
        Map<String, Object> fakeMap = new HashMap<>();

        fakeMap.put("thisIsAnInvalidAttributeKeyName", attributeNames);
        fakeMap.put("notResults", results);
        return fakeMap;
    }

    private Map<String, Object> buildMapWithEmptyLists() {
        List<String> attributeNames = new ArrayList<>(1);

        List<List<String>> results = new ArrayList<>(1);
        Map<String, Object> fakeMap = new HashMap<>();

        fakeMap.put("attributeNames", attributeNames);
        fakeMap.put("results", results);
        return fakeMap;
    }

}
