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

package org.maestro.inspector.activemq.converter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.maestro.inspector.activemq.JolokiaConverter;

import java.util.Map;

public class MapConverter implements JolokiaConverter {
    private final Map<String, Object> properties;


    public MapConverter(Map<String, Object> properties) {
        this.properties = properties;
    }

    protected Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void convert(String propertyName, Object object) {
        if (object instanceof JSONObject) {
            convert(propertyName, (JSONObject) object);

            return;
        }

        if (object instanceof Number) {
            convert(propertyName, (Number) object);

            return;
        }

        if (object instanceof String) {
            convert(propertyName, (String) object);

            return;
        }

        if (object instanceof Boolean) {
            convert(propertyName, (Boolean) object);
            return;
        }

        // TODO: support JSON array
        if (object instanceof JSONArray) {
            return;
        }

        if (object == null) {
            properties.put(propertyName, object);
            return;
        }

        throw new IllegalArgumentException("The input object of type " + object.getClass() + " for property " +
                propertyName + " cannot be converted to a JSONObject");
    }

    @Override
    public void convert(String propertyName, JSONObject jsonObject) {
        properties.put(propertyName, jsonObject.get("value"));
    }

    public <T extends Number> void convert(String propertyName, T number) {
        properties.put(propertyName, number);
    }

    public void convert(String propertyName, String value) {
        properties.put(propertyName, value);
    }

    public void convert(String propertyName, Boolean value) {
        properties.put(propertyName, value);
    }
}
