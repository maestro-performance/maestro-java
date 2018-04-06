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

package org.maestro.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class to load properties into a Map
 */
public class PropertyUtils {
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);


    /**
     * Loads the contents of test/broker/etc properties file into a map
     * @param testProperties the properties file to load
     * @param context the map that will receive the properties content
     */
    public static void loadProperties(final File testProperties, Map<String, Object> context) {
        if (testProperties.exists()) {
            Properties prop = new Properties();

            try (FileInputStream in = new FileInputStream(testProperties)) {
                prop.load(in);

                prop.forEach((key, value) -> {
                    logger.trace("Adding entry {} with value {}", key, value);
                    context.put((String) key, value);
                });
            } catch (FileNotFoundException e) {
                logger.error("File not found error: {}", e.getMessage(), e);
            } catch (IOException e) {
                logger.error("Input/output error: {}", e.getMessage(), e);
            }
        }
        else {
            logger.debug("There are no properties file at {}", testProperties.getPath());
        }
    }

}
