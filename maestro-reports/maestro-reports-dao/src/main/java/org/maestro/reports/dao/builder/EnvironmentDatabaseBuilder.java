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

package org.maestro.reports.dao.builder;

import org.apache.commons.dbcp2.BasicDataSource;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.dao.TemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The template builder that creates a JDBC template for external databases that read settings from environment
 * variables
 */
public class EnvironmentDatabaseBuilder implements TemplateBuilder {
    protected static BasicDataSource ds;

    private void checkVar(final String varName, final String value) {
        if (value == null) {
            throw new MaestroException("The application is reading DB information from the environment but the %s " +
                    "variable is not set", varName);
        }
    }

    @Override
    public JdbcTemplate build() {
        if (ds == null) {
            synchronized (this) {
                if (ds == null) {
                    try {
                        ds = new BasicDataSource();

                        final String driverClassName = System.getenv("MAESTRO_REPORTS_DRIVER");
                        checkVar(driverClassName, "MAESTRO_REPORTS_DRIVER");

                        ds.setDriverClassName(driverClassName);

                        final String url = System.getenv("MAESTRO_REPORTS_DATASOURCE_URL");
                        checkVar(url, "MAESTRO_REPORTS_DATASOURCE_URL");

                        ds.setUrl(url);

                        final String username = System.getenv("MAESTRO_REPORTS_DATASOURCE_USERNAME");
                        checkVar(username, "MAESTRO_REPORTS_DATASOURCE_URL");

                        ds.setUsername(username);

                        final String password = System.getenv("MAESTRO_REPORTS_DATASOURCE_PASSWORD");
                        checkVar(password, "MAESTRO_REPORTS_DATASOURCE_URL");

                        ds.setPassword(password);

                        ds.setInitialSize(2);

                        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
                        jdbcTemplate.update("select 1 from dual");

                        return jdbcTemplate;
                    }
                    catch (Throwable t) {
                        Logger logger = LoggerFactory.getLogger(EnvironmentDatabaseBuilder.class);
                        logger.error("Unable to connect to an external DB: {}", t.getMessage(), t);

                        throw new MaestroException(t);
                    }
                }
            }
        }

        return new JdbcTemplate(ds);
    }
}
