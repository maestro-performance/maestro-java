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

package org.maestro.plotter.common.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * A simple converter for writing data types to properties
 */
public class DefaultConverter implements PropertyConverter {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConverter.class);

    public void write(final Properties prop, final String propertyName, final Object object) {
        if (object == null) {
            logger.error("Cannot convert a 'null' object for saving as a property");

            return;
        }

        if (object instanceof Integer) {
            Integer value = (Integer) object;
            prop.setProperty(propertyName, Integer.toString(value));

            return;
        }

        if (object instanceof Double) {
            Double value = (Double) object;
            prop.setProperty(propertyName, Double.toString(value));

            return;
        }

        if (object instanceof Long) {
            Long value = (Long) object;
            prop.setProperty(propertyName, Long.toString(value));

            return;
        }

        if (object instanceof String) {
            prop.setProperty(propertyName, (String) object);

            return;
        }
    }

}
