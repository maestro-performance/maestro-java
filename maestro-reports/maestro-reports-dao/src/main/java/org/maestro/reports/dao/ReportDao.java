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

import org.maestro.reports.dto.Report;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.List;

public class ReportDao extends AbstractDao {

    public int insert(final Report report) {
        return runInsert(
                "insert into report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result, location) " +
                        "values(:testId, :testNumber, :testName, :testScript, :testHost, :testHostRole, :testResult, :location)", report);
    }

    public List<Report> fetch() {
        return jdbcTemplate.query("select * from report",
                new BeanPropertyRowMapper<>(Report.class));
    }

    public Report fetch(int reportId) {
        return jdbcTemplate.queryForObject("select * from report where report_id = ?",
                new Object[]{ reportId },
                new BeanPropertyRowMapper<>(Report.class));
    }

    public List<Report> fetch(int testId, int testNumber) {
        return jdbcTemplate.query("select * from report where test_id = ? and test_number = ? ",
                new Object[]{ testId, testNumber },
                new BeanPropertyRowMapper<>(Report.class));
    }
}
