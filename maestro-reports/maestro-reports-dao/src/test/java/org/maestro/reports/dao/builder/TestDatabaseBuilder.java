/*
 * Copyright 2018 Otavio Rodolfo Piske
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.io.File;
import java.sql.SQLException;

public class TestDatabaseBuilder implements TemplateBuilder {
    protected static BasicDataSource ds;
    private final String name;


    public TestDatabaseBuilder(String name) {
        this.name = name;
    }

    @Override
    public JdbcTemplate build() {
        if (ds == null) {
            synchronized (this) {
                if (ds == null) {
                    ds = new BasicDataSource();

                    final String driverClassName = "org.h2.Driver";
                    ds.setDriverClassName(driverClassName);

                    final String local = this.getClass().getResource(".").getPath() + File.separator + name;
                    final String url = "jdbc:h2:" + local;
                    ds.setUrl(url);

                    ds.setInitialSize(2);

                    JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

                    try {
                        InputStreamResource resource = new InputStreamResource(this.getClass().getResourceAsStream("create-db.sql"));

                        ScriptUtils.executeSqlScript(ds.getConnection(), resource);
                    } catch (SQLException e) {
                        throw new MaestroException("Unable to create the reports table");
                    }

                    try {
                        InputStreamResource resource = new InputStreamResource(this.getClass().getResourceAsStream("data.sql"));

                        ScriptUtils.executeSqlScript(ds.getConnection(), resource);
                    } catch (SQLException e) {
                        throw new MaestroException("Unable to truncate the reports table");
                    }


                    return jdbcTemplate;
                }
            }
        }

        return new JdbcTemplate(ds);
    }
}
