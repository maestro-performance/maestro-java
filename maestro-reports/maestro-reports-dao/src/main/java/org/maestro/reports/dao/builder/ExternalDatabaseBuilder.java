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

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.dao.TemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * The template builder that creates a JDBC template for external databases
 */
public class ExternalDatabaseBuilder implements TemplateBuilder {
    protected static BasicDataSource ds;

    @Override
    public JdbcTemplate build() {
        if (ds == null) {
            synchronized (this) {
                if (ds == null) {
                    try {
                        ds = new BasicDataSource();

                        AbstractConfiguration config = ConfigurationWrapper.getConfig();

                        final String driverClassName = config.getString("maestro.reports.driver");
                        ds.setDriverClassName(driverClassName);

                        final String url = config.getString("maestro.reports.datasource.url");
                        ds.setUrl(url);

                        final String username = config.getString("maestro.reports.datasource.username");
                        ds.setUsername(username);

                        final String password = config.getString("maestro.reports.datasource.password");
                        ds.setPassword(password);

                        ds.setInitialSize(2);

                        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
                        jdbcTemplate.update("select 1 from dual");

                        return jdbcTemplate;
                    }
                    catch (Throwable t) {
                        Logger logger = LoggerFactory.getLogger(ExternalDatabaseBuilder.class);
                        logger.error("Unable to connect to an external DB: {}", t.getMessage(), t);

                        throw new MaestroException(t);
                    }
                }
            }
        }

        return new JdbcTemplate(ds);
    }
}
