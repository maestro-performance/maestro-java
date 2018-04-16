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
import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.maestro.plotter.common.statistics.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Writes rate data properties to a file
 */
public class PropertyWriter<T extends ReportData> {
    private static final Logger logger = LoggerFactory.getLogger(PropertyWriter.class);

    private PropertyWriter() {}

    /**
     * Dump the data as a property file (if properly annotated)
     * @param data the data to save
     * @param outputFile the output file
     * @throws IOException if unable to save
     */
    public static void write(final Object data, final File outputFile) throws IOException {
        logger.trace("Writing properties to {}", outputFile.getPath());

        if (data == null) {
            logger.warn("Cannot dump properties for a null bean");
            return;
        }

        Properties prop = new Properties();

        PropertyName propertyNameAnnotation = data.getClass().getAnnotation(PropertyName.class);
        if (propertyNameAnnotation == null) {
            logger.error("Trying to dump the properties for a class {} but it is not properly annotated",
                    data.getClass());

            return;
        }
        String classPropertyName = propertyNameAnnotation.name();
        logger.error("Inspecting the properties for {} on {}",classPropertyName, data.getClass());

        Method[] methods = data.getClass().getMethods();

        for (Method method : methods) {

            if (method.isAnnotationPresent(PropertyProvider.class)) {
                try {
                    Object ret = method.invoke(data, null);
                    logger.debug("Obtained: " + ret);

                    PropertyProvider methodProperty = method.getAnnotation(PropertyProvider.class);
                    if (ret instanceof Statistics) {
                        Statistics statistics = (Statistics) ret;
                        String basePropertyName = classPropertyName
                                + StringUtils.capitalize(methodProperty.name());

                        prop.setProperty(basePropertyName + "Max", Double.toString(statistics.getMax()));
                        prop.setProperty(basePropertyName + "Min", Double.toString(statistics.getMin()));
                        prop.setProperty(basePropertyName + "GeometricMean", Double.toString(statistics.getGeometricMean()));
                        prop.setProperty(basePropertyName + "StandardDeviation", Double.toString(statistics.getStandardDeviation()));
                    }

                } catch (IllegalAccessException e) {
                    logger.error("Illegal access to method {}: {}", method, e.getMessage());
                } catch (InvocationTargetException e) {
                    logger.error("Unable to call method {}: {}", method, e.getMessage());
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            prop.store(fos, "maestro-plotter");
        }
    }


}
