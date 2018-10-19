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

import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import java.util.List;


/**
 * Base database access object for the reports code
 */
public abstract class AbstractDao extends NamedParameterJdbcDaoSupport {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDao.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor
     */
    protected AbstractDao() {
        this(TemplateBuilderManager.getTemplateBuilder());
    }


    /**
     * Constructor with custom JDBC template builder
     * @param tp JDBC template builder
     */
    protected AbstractDao(final TemplateBuilder tp) {
        super();

        logger.trace("Building the JDBC template");
        jdbcTemplate = tp.build();
        super.setJdbcTemplate(jdbcTemplate);
    }


    /**
     * Runs a DB query returning a single object
     * @param query the query to run
     * @param rowMapper the Spring JDBC row mapper
     * @param id the ID
     * @param <T> the type of the bean being returned
     * @return A record for the query matching the specified type of bean
     * @throws DataNotFoundException if the records are not found
     */
    protected <T> T runQuery(final String query, final RowMapper<T> rowMapper, final Object id) throws DataNotFoundException
    {
        return EasyRunner.runQuery(jdbcTemplate, query, rowMapper, id);
    }


    /**
     * Runs a DB query returning a single object
     * @param query the query to run
     * @param rowMapper the Spring JDBC row mapper
     * @param args the arguments for the query
     * @param <T> the type of the bean being returned
     * @return A record for the query matching the specified type of bean
     * @throws DataNotFoundException if the records are not found
     */
    protected <T> T runQuery(final String query, final RowMapper<T> rowMapper, final Object...args)
            throws DataNotFoundException
    {
        return EasyRunner.runQuery(jdbcTemplate, query, rowMapper, args);
    }


    /**
     * Runs a DB query returning multiple records
     * @param query the query to run
     * @param rowMapper the Spring JDBC row mapper
     * @param <T> the type of the bean being returned
     * @return A record for the query matching the specified type of bean
     * @throws DataNotFoundException if the records are not found
     */
    protected <T> List<T> runQueryMany(final String query, final RowMapper<T> rowMapper) throws DataNotFoundException {
        return EasyRunner.runQueryMany(jdbcTemplate, query, rowMapper);
    }


    /**
     * Runs a DB query returning multiple records
     * @param query the query to run
     * @param rowMapper the Spring JDBC row mapper
     * @param args query arguments
     * @param <T> the type of the bean being returned
     * @return A record for the query matching the specified type of bean
     * @throws DataNotFoundException if the records are not found
     */
    protected <T> List<T> runQueryMany(final String query, final RowMapper<T> rowMapper, final Object...args)
            throws DataNotFoundException {
        return EasyRunner.runQueryMany(jdbcTemplate, query, rowMapper, args);
    }


    /**
     * Update records in the DB
     * @param query the query to run
     * @param bean the bean with the data to update
     */
    protected void runUpdate(final String query, final Object bean) {
        EasyRunner.runUpdate(jdbcTemplate, query, bean);
    }


    /**
     * Update records in the DB
     * @param query the query to run
     * @param args update arguments
     * @return the ID of the record updated
     */
    protected int runUpdate(final String query, final Object...args) {
        return EasyRunner.runUpdate(jdbcTemplate, query, args);
    }


    /**
     * Inserts a record in the DB
     * @param query the query to run
     * @param bean the bean with the data to insert
     */
    protected void runEmptyInsert(final String query, final Object bean) {
        EasyRunner.runEmptyInsert(getNamedParameterJdbcTemplate(), query, bean);
    }


    /**
     * Inserts a record in the DB
     * @param query the query to run
     * @param bean the bean with the data to insert
     * @return the ID of the record updated
     */
    protected int runInsert(final String query, final Object bean) {
        return EasyRunner.runInsert(getNamedParameterJdbcTemplate(), query, bean);
    }

}
