package org.maestro.inspector.activemq;

import org.json.simple.JSONObject;

public interface JolokiaConverter {

    default void convert(final String propertyName, Object object) {
        if (object instanceof JSONObject) {
            convert(propertyName, (JSONObject) object);

            return;
        }

        throw new IllegalArgumentException("The input object of type " + (object == null ? "'null'" : object.getClass())
                + " for property " +  propertyName + " cannot be converted to a JSONObject");
    }

    void convert(final String propertyName, JSONObject jsonObject);
}
