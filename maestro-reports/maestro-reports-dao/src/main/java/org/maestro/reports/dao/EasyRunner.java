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

package org.maestro.reports.dao;

import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;

/**
 * Utility code to run DB queries. Wrapper for Spring JDBC code
 */
public final class EasyRunner {

    private EasyRunner() {}

    /**
     * Runs a DB query returning a single object
     * @param jdbcTemplate the Spring JDBC template object
     * @param query the query to run
     * @param rowMapper the Spring JDBC row mapper
     * @param id the ID
     * @param <T> the type of the bean being returned
     * @return A record for the query matching the specified type of bean
     * @throws DataNotFoundException if the records are not found
     */
    public static <T> T runQuery(final JdbcTemplate jdbcTemplate, final String query, final RowMapper<T> rowMapper,
                                 final Object id) throws DataNotFoundException {
        try {
            return jdbcTemplate.queryForObject(query, rowMapper, id);
        }
        catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("A record with ID " + id + " was not found in the DB", e);
        }
    }


    /**
     * Runs a DB query returning a single object
     * @param jdbcTemplate the Spring JDBC template object
     * @param query the query to run
     * @param rowMapper the Spring JDBC row mapper
     * @param args the arguments for the query
     * @param <T> the type of the bean being returned
     * @return A record for the query matching the specified type of bean
     * @throws DataNotFoundException if the records are not found
     */
    public static <T> T runQuery(final JdbcTemplate jdbcTemplate, final String query, final RowMapper<T> rowMapper,
                                 final Object...args) throws DataNotFoundException
    {
        try {
            return jdbcTemplate.queryForObject(query, rowMapper, args);
        }
        catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("No matching record for the query was not found in the DB", e);
        }
    }


    /**
     * Runs a DB query returning multiple records
     * @param jdbcTemplate the Spring JDBC template object
     * @param query the query to run
     * @param rowMapper the Spring JDBC row mapper
     * @param <T> the type of the bean being returned
     * @return A record for the query matching the specified type of bean
     * @throws DataNotFoundException if the records are not found
     */
    public static <T> List<T> runQueryMany(final JdbcTemplate jdbcTemplate, final String query,
                                           final RowMapper<T> rowMapper) throws DataNotFoundException
    {
        try {
            return jdbcTemplate.query(query, rowMapper);
        }
        catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("No matching record for the query was not found in the DB", e);
        }
    }


    /**
     * Runs a DB query returning multiple records
     * @param jdbcTemplate the Spring JDBC template object
     * @param query the query to run
     * @param rowMapper the Spring JDBC row mapper
     * @param args query arguments
     * @param <T> the type of the bean being returned
     * @return A record for the query matching the specified type of bean
     * @throws DataNotFoundException if the records are not found
     */
    public static <T> List<T> runQueryMany(final JdbcTemplate jdbcTemplate, final String query,
                                           final RowMapper<T> rowMapper, final Object...args) throws DataNotFoundException
    {
        try {
            return jdbcTemplate.query(query, rowMapper, args);
        }
        catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("No matching record for the query was not found in the DB", e);
        }
    }


    /**
     * Update records in the DB
     * @param jdbcTemplate the Spring JDBC template object
     * @param query the query to run
     * @param bean the bean with the data to update
     */
    public static void runUpdate(final JdbcTemplate jdbcTemplate, final String query, final Object bean) {
        SqlParameterSource beanParameters = new BeanPropertySqlParameterSource(bean);

        jdbcTemplate.update(query, beanParameters);
    }


    /**
     * Update records in the DB
     * @param jdbcTemplate the Spring JDBC template object
     * @param query the query to run
     * @param args update arguments
     * @return the ID of the record updated
     */
    public static int runUpdate(final JdbcTemplate jdbcTemplate, final String query, final Object...args) {
        return jdbcTemplate.update(query, args);
    }


    /**
     * Inserts a record in the DB
     * @param jdbcTemplate the Spring JDBC template object
     * @param query the query to run
     * @param bean the bean with the data to insert
     */
    public static void runEmptyInsert(final NamedParameterJdbcTemplate jdbcTemplate, final String query, final Object bean) {
        SqlParameterSource beanParameters = new BeanPropertySqlParameterSource(bean);

        jdbcTemplate.update(query, beanParameters);
    }


    /**
     * Inserts a record in the DB
     * @param jdbcTemplate the Spring JDBC template object
     * @param query the query to run
     * @param bean the bean with the data to insert
     * @return the ID of the record updated
     */
    public static int runInsert(final NamedParameterJdbcTemplate jdbcTemplate, final String query, final Object bean) {
        SqlParameterSource beanParameters = new BeanPropertySqlParameterSource(bean);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(query, beanParameters, keyHolder);
        return keyHolder.getKey().intValue();
    }
}
