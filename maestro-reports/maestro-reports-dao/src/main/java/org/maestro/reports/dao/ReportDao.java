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

    public void insert(final Report report) {
        runEmptyInsert(
                "insert into report(test_id, test_number, test_name, test_script, test_host, test_host_role, " +
                        "test_result, location, aggregated, test_description, test_comments, valid, retired, " +
                        "retired_date, test_date) " +
                        "values(:testId, :testNumber, :testName, :testScript, :testHost, :testHostRole, :testResult, " +
                        ":location, :aggregated, :testDescription, :testComments, :valid, :retired, :retiredDate, :testDate)",
                report);
    }

    public int update(final Report report) {
        return runUpdate("update report set test_id = ?, test_number = ?, test_name = ?, test_script = ?, test_host = ?, " +
                "test_host_role = ?, test_result = ?, location = ?, aggregated = ?, test_description = ?, " +
                "test_comments = ?, valid = ?, retired = ?, retired_date = ?, test_date = ? where report_id = ?",
                report.getTestId(), report.getTestNumber(), report.getTestName(), report.getTestScript(),
                report.getTestHost(), report.getTestHostRole(), report.getTestResult(), report.getLocation(),
                report.isAggregated(), report.getTestDescription(), report.getTestComments(), report.isValid(),
                report.isRetired(), report.getRetiredDate(), report.getTestDate(), report.getReportId());
    }

    /**
     * Fetch all non-aggregated records
     * @return A list of records
     * @throws DataNotFoundException
     */
    public List<Report> fetch() throws DataNotFoundException {
        return runQueryMany("select * from report where aggregated = false and valid = true",
                new BeanPropertyRowMapper<>(Report.class));
    }

    /**
     * Fetch all records regardless of any status
     * @return A list of records
     * @throws DataNotFoundException
     */
    public List<Report> fetchAll() throws DataNotFoundException {
        return runQueryMany("select * from report", new BeanPropertyRowMapper<>(Report.class));
    }

    /**
     * Fetch by report ID
     * @param reportId
     * @return
     * @throws DataNotFoundException
     */
    public Report fetch(int reportId) throws DataNotFoundException {
        return runQuery("select * from report where aggregated = false and report_id = ?",
                new BeanPropertyRowMapper<>(Report.class),
                reportId);
    }

    /**
     * Fetch by test ID and test number
     * @param testId
     * @param testNumber
     * @return
     * @throws DataNotFoundException
     */
    public List<Report> fetch(int testId, int testNumber) throws DataNotFoundException {
        return runQueryMany("select * from report where aggregated = false and test_id = ? and test_number = ? ",
                new BeanPropertyRowMapper<>(Report.class),
                testId, testNumber);
    }

    /**
     * Fetch by test ID
     * @param testId
     * @return
     * @throws DataNotFoundException
     */
    public List<Report> fetchByTestId(int testId) throws DataNotFoundException {
        return runQueryMany("select * from report where test_id = ?", new BeanPropertyRowMapper<>(Report.class),
                testId);
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
