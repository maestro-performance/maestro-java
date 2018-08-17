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

import org.maestro.common.StringUtils;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * Writes rate data properties to a file
 */
public class PropertyWriter {
    private static final Logger logger = LoggerFactory.getLogger(PropertyWriter.class);
    private final PropertyConverter converter = new DefaultConverter();


    private boolean canHandle(Object object) {
        if (Number.class.isAssignableFrom(object.getClass())) {
            return true;
        }

        return object instanceof String;
    }

    private void saveProperties(Object data, Properties prop, final String propertyName) {
        Method[] methods = data.getClass().getMethods();

        for (Method method : methods) {

            if (method.isAnnotationPresent(PropertyProvider.class)) {
                try {
                    Object ret = method.invoke(data);

                    if (logger.isTraceEnabled()) {
                        logger.trace("Obtained: {} ", ret);
                    }

                    PropertyProvider methodProperty = method.getAnnotation(PropertyProvider.class);

                    String newPropertyName = propertyName + StringUtils.capitalize(methodProperty.name());

                    if (canHandle(ret)) {
                        converter.write(prop, newPropertyName, ret);
                    }
                    else {
                        if (ret instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) ret;

                            map.forEach((key, value) -> mapIterator(prop, newPropertyName, (String) key, value));
                        }
                        else {
                            saveProperties(ret, prop, newPropertyName);
                        }

                    }

                } catch (IllegalAccessException e) {
                    logger.error("Illegal access to method {}: {}", method, e.getMessage());
                } catch (InvocationTargetException e) {
                    logger.error("Unable to call method {}: {}", method, e.getMessage());
                }
            }
        }


    }

    private void mapIterator(Properties prop, String newPropertyName, String key, Object value) {
        String sanitizedName = StringUtils.capitalize(key).replace(" ", "");
        String combinedName = newPropertyName + sanitizedName;

        try {
            saveProperties(value, prop, combinedName);
        } catch (Throwable t) {
            logger.error("Unable to save property {}: {}", combinedName, t);
        }
    }

    /**
     * Dump the data as a property file (if properly annotated)
     * @param data the data to save
     * @param outputFile the output file
     * @throws IOException if unable to save
     */
    public void write(final Object data, final File outputFile) throws IOException {
        logger.trace("Writing properties to {}", outputFile.getPath());

        if (data == null) {
            logger.warn("Cannot dump properties for a null bean");
            return;
        }

        PropertyName propertyNameAnnotation = data.getClass().getAnnotation(PropertyName.class);
        if (propertyNameAnnotation == null) {
            logger.error("Trying to dump the properties for a class {} but it is not properly annotated",
                    data.getClass());

            return;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Inspecting the properties for {} on {}", propertyNameAnnotation.name(), data.getClass());
        }

        Properties prop = new Properties();

        saveProperties(data, prop, propertyNameAnnotation.name());

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            prop.store(fos, "maestro-plotter");
        }
    }
}
