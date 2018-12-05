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

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The template builder interface can be used to implement different ways
 * of creating the Spring JDBC template used to run the queries. It can be
 * used to implement support for different databases as well as unit test
 * cases for the DAO code
 */
public interface TemplateBuilder {

    /**
     * Build a Spring JDBC template object
     * @return a Spring JDBC template object
     */
    JdbcTemplate build();
}
