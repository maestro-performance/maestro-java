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

package org.maestro.reports.dao;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.reports.dao.builder.ExternalDatabaseBuilder;
import org.maestro.reports.dao.builder.InternalDatabaseBuilder;

public final class TemplateBuilderManager {

    static TemplateBuilder getTemplateBuilder() {
        AbstractConfiguration config = ConfigurationWrapper.getConfig();

        String type = config.getString("maestro.reports.db.type", "internal");

        if (type.equals("internal")) {
            return new InternalDatabaseBuilder();
        }

        return new ExternalDatabaseBuilder();
    }

}
