package org.maestro.inspector.activemq;

public class JolokiaUtils {
    public static long getLong(Object object, long defaultValue) {
        if (object == null) {
            return defaultValue;
        }

        if (object instanceof Long) {
            return (Long) object;
        }

        return defaultValue;
    }

    public static long getLong(Object object) {
        return getLong(object, 0);
    }
}
