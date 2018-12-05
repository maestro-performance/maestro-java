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
 *
 */

package org.maestro.common.test.properties;

import org.maestro.common.test.properties.annotations.PropertyConsumer;
import org.maestro.common.test.properties.annotations.PropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import static org.maestro.common.test.properties.PropertyUtils.getPropertyName;

public class PropertyReader {
    private static final Logger logger = LoggerFactory.getLogger(PropertyReader.class);

    private void set(final Object data, final String beanPropertyName, final String propertyName, final Object val) {
        Method[] methods = data.getClass().getMethods();

        for (Method method : methods) {

            if (method.isAnnotationPresent(PropertyConsumer.class)) {
                try {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Inspecting: {} ", method.getName());
                    }

                    PropertyConsumer methodProperty = method.getAnnotation(PropertyConsumer.class);

                    String newPropertyName = getPropertyName(beanPropertyName, methodProperty.name(), methodProperty.join());

                    if (newPropertyName.equals(propertyName)) {
                        method.invoke(data, val);

                        return;
                    }
                } catch (IllegalAccessException e) {
                    logger.error("Illegal access to method {}: {}", method, e.getMessage());
                } catch (InvocationTargetException e) {
                    logger.error("Unable to call method {}: {}", method, e.getMessage());
                }
            }
        }


    }

    private void set(final Object bean, final Properties properties, final String key) {
        Object value = properties.get(key);

        PropertyName propertyNameAnnotation = bean.getClass().getAnnotation(PropertyName.class);
        if (propertyNameAnnotation == null) {
            logger.error("Trying to dump the properties for a class {} but it is not properly annotated",
                    bean.getClass());

            return;
        }

        set(bean, propertyNameAnnotation.name(), key, value);

    }

    public void read(final File testProperties, final Object bean) throws IOException {
        logger.debug("Reading properties from {}", testProperties.getPath());

        Properties prop = new Properties();

        try (FileInputStream in = new FileInputStream(testProperties)) {
            prop.load(in);

            prop.keySet().forEach(k -> set(bean, prop, (String) k));

        } catch (Throwable t) {
            logger.error("Invalid data when processing file {}", testProperties.getPath(), t);
            throw t;
        }

        logger.debug("Read properties: {}", this.toString());
    }
}
