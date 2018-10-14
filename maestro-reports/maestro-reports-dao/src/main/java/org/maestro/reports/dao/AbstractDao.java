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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;


public abstract class AbstractDao extends NamedParameterJdbcDaoSupport {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDao.class);

    private JdbcTemplate jdbcTemplate = null;

    protected AbstractDao() {
        super();

        TemplateBuilder tp = TemplateBuilderManager.getTemplateBuilder();

        jdbcTemplate = tp.build();
        super.setJdbcTemplate(jdbcTemplate);
    }

    protected <T> T runQuery(String query, RowMapper<T> rowMapper, Object id) throws DataNotFoundException {
        try {
            return jdbcTemplate.queryForObject(query, rowMapper, id);
        }
        catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("A record with ID " + id + " was not found in the DB", e);
        }
    }

    protected <T> T runQuery(String query, RowMapper<T> rowMapper, Object...args) throws DataNotFoundException {
        try {
            return jdbcTemplate.queryForObject(query, rowMapper, args);
        }
        catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("No matching record for the query was not found in the DB", e);
        }
    }

    protected <T> List<T> runQueryMany(String query, RowMapper<T> rowMapper) throws DataNotFoundException {
        try {
            return jdbcTemplate.query(query, rowMapper);
        }
        catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("No matching record for the query was not found in the DB", e);
        }
    }

    protected <T> List<T> runQueryMany(String query, RowMapper<T> rowMapper, Object id) throws DataNotFoundException {
        try {
            return jdbcTemplate.query(query, rowMapper, id);
        }
        catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("A record with ID " + id + " was not found in the DB", e);
        }
    }

    protected void runUpdate(final String query, Object o) {
        SqlParameterSource beanParameters = new BeanPropertySqlParameterSource(o);

        getNamedParameterJdbcTemplate().update(query, beanParameters);
    }

    protected int runUpdate(final String query, Object...args) {
        return jdbcTemplate.update(query, args);
    }

    protected void runEmptyInsert(final String query, Object o) {
        SqlParameterSource beanParameters = new BeanPropertySqlParameterSource(o);

        getNamedParameterJdbcTemplate().update(query, beanParameters);
    }


    protected int runInsert(final String query, Object o) {
        SqlParameterSource beanParameters = new BeanPropertySqlParameterSource(o);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        getNamedParameterJdbcTemplate().update(query, beanParameters, keyHolder);
        return keyHolder.getKey().intValue();
    }

}
