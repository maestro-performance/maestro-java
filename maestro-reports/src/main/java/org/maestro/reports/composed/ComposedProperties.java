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

package org.maestro.reports.composed;

import org.maestro.common.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ComposedProperties {
    private static final Logger logger = LoggerFactory.getLogger(ComposedProperties.class);
    private final File indexProperties;
    private String dateTime;
    private final Map<String, Object> context = new HashMap<>();

    public ComposedProperties(final File indexProperties) {
        this.indexProperties = indexProperties;

        PropertyUtils.loadProperties(indexProperties, context);

        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(indexProperties.getParentFile().toPath(), BasicFileAttributes.class);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/YYYY HH:mm:ss z")
                            .withLocale(Locale.getDefault())
                                                        .withZone(ZoneId.systemDefault());

            dateTime = formatter.format(attr.creationTime().toInstant());
        } catch (IOException e) {
            logger.error("Unable to read file creation time for {}: {}", indexProperties, e.getMessage(), e);
            dateTime = "0000";
        }

    }

    public String getParentDir() {
        return indexProperties.getParentFile().getName();
    }

    public String getDateTime() {
        return dateTime;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
