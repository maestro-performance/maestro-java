package org.maestro.inspector.activemq.converter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.maestro.inspector.activemq.JolokiaConverter;

import java.util.Map;

public class MapConverter implements JolokiaConverter {
    private Map<String, Object> properties;


    public MapConverter(Map<String, Object> properties) {
        this.properties = properties;
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

        throw new IllegalArgumentException("The input object of type " + (object == null ? "'null'" : object.getClass())
                + " for property " +  propertyName + " cannot be converted to a JSONObject");
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
