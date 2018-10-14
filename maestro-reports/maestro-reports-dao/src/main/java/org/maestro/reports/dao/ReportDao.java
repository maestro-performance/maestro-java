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
import org.maestro.reports.dto.Report;
import org.maestro.reports.dto.ReportAggregationInfo;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.List;

/**
 * DAO for the reports table
 */
public class ReportDao extends AbstractDao {

    public ReportDao() {}

    public ReportDao(TemplateBuilder tp) {
        super(tp);
    }

    public int insert(final Report report) {
        return runInsert(
                "insert into report(test_id, test_number, test_name, test_script, test_host, test_host_role, " +
                        "test_result, location, aggregated) values(:testId, :testNumber, :testName, :testScript, " +
                        ":testHost, :testHostRole, :testResult, :location, :aggregated)", report);
    }

    public List<Report> fetch() throws DataNotFoundException {
        return runQueryMany("select * from report where aggregated = false",
                new BeanPropertyRowMapper<>(Report.class));
    }

    public Report fetch(int reportId) throws DataNotFoundException {
        return runQuery("select * from report where aggregated = false and report_id = ?",
                new BeanPropertyRowMapper<>(Report.class),
                reportId);
    }

    public List<Report> fetch(int testId, int testNumber) throws DataNotFoundException {
        return runQueryMany("select * from report where aggregated = false and test_id = ? and test_number = ? ",
                new BeanPropertyRowMapper<>(Report.class),
                testId, testNumber);
    }

    public Report fetchAggregated(int testId, int testNumber) throws DataNotFoundException {
        return runQuery(
                "select * from report where aggregated = true and test_id = ? and test_number = ?",
                new BeanPropertyRowMapper<>(Report.class),
                testId, testNumber);
    }

    public List<ReportAggregationInfo> aggregationInfo() throws DataNotFoundException {
        return runQueryMany("SELECT test_id,test_number,sum(aggregated) AS aggregations FROM report " +
                "GROUP BY test_id,test_number ORDER BY test_id desc",
                new BeanPropertyRowMapper<>(ReportAggregationInfo.class));
    }
}
