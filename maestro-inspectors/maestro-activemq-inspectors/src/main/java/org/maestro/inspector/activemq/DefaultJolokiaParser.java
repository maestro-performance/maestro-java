package org.maestro.inspector.activemq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse the Jolokia properties and convert them to inspector types
 */
public class DefaultJolokiaParser {
    private static final Logger logger = LoggerFactory.getLogger(DefaultJolokiaParser.class);

    public DefaultJolokiaParser() {
    }

    /**
     * Parse a jolokia property
     * @param converter
     * @param key
     * @param object
     */
    void parse(final JolokiaConverter converter, final Object key, final Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace("Processing returned JSON Key {} with value: {}", key, object);
        }

        String jolokiaPropertyName = "";
        if (key instanceof String) {
            String tmp = (String) key;
            logger.debug("Checking property name/group {}", tmp);

            Pattern pattern = Pattern.compile(".*name=(.*),.*");
            Matcher matcher = pattern.matcher(tmp);

            if (matcher.matches()) {
                jolokiaPropertyName = matcher.group(1);

                logger.debug("Reading property name/group '{}'", jolokiaPropertyName);
            } else {
                jolokiaPropertyName = tmp;
            }
        }

        converter.convert(jolokiaPropertyName, object);
    }
}