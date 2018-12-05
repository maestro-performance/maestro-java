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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.util.List;

/**
 * DAO for the reports table
 */
public class ReportDao extends AbstractDao {
    private static final Logger logger = LoggerFactory.getLogger(ReportDao.class);

    /**
     * Constructor
     */
    public ReportDao() {}

    /**
     * Constructor
     * @param tp the Spring JDBC template builder
     */
    public ReportDao(TemplateBuilder tp) {
        super(tp);
    }

    /**
     * Inserts a new record into the DB
     * @param report the record to insert
     */
    public void insert(final Report report) {
        runEmptyInsert(
                "insert into report(test_id, test_number, test_name, test_script, test_host, test_host_role, " +
                        "test_result, test_result_message, location, aggregated, test_description, test_comments, " +
                        "valid, retired, retired_date, test_date) " +
                        "values(:testId, :testNumber, :testName, :testScript, :testHost, :testHostRole, :testResult, " +
                        ":testResultMessage, :location, :aggregated, :testDescription, :testComments, :valid, " +
                        ":retired, :retiredDate, :testDate)",
                report);
    }


    /**
     * Updates a record on the DB (by report ID)
     * @param report the record to update
     * @return the index of the updated record
     */
    public int update(final Report report) {
        return runUpdate("update report set test_id = ?, test_number = ?, test_name = ?, test_script = ?, " +
                "test_host = ?, test_host_role = ?, test_result = ?, test_result_message = ?, location = ?, " +
                "aggregated = ?, test_description = ?, test_comments = ?, valid = ?, retired = ?, retired_date = ?, " +
                "test_date = ? where report_id = ?",
                report.getTestId(), report.getTestNumber(), report.getTestName(), report.getTestScript(),
                report.getTestHost(), report.getTestHostRole(), report.getTestResult(), report.getTestResultMessage(),
                report.getLocation(), report.isAggregated(), report.getTestDescription(), report.getTestComments(),
                report.isValid(),report.isRetired(), report.getRetiredDate(), report.getTestDate(), report.getReportId());
    }

    /**
     * Fetch all non-aggregated records
     * @return A list of records
     * @throws DataNotFoundException if no records are found that match the query
     */
    public List<Report> fetch() throws DataNotFoundException {
        return runQueryMany("select * from report where aggregated = false and valid = true order by report_id desc",
                new BeanPropertyRowMapper<>(Report.class));
    }

    /**
     * Fetch all records regardless of any status
     * @return A list of records
     * @throws DataNotFoundException if no records are found that match the query
     */
    public List<Report> fetchAll() throws DataNotFoundException {
        return runQueryMany("select * from report", new BeanPropertyRowMapper<>(Report.class));
    }


    /**
     * Fetch all records regardless of any status
     * @return A list of records
     * @throws DataNotFoundException if no records are found that match the query
     */
    public List<Report> fetchAllAggregated() throws DataNotFoundException {
        return runQueryMany("select * from report where aggregated = true and valid = true", new BeanPropertyRowMapper<>(Report.class));
    }

    /**
     * Fetch by report ID
     * @param reportId the report ID
     * @return the record matching the report ID
     * @throws DataNotFoundException if no records are found that match the query
     */
    public Report fetch(int reportId) throws DataNotFoundException {
        return runQuery("select * from report where aggregated = false and report_id = ?",
                new BeanPropertyRowMapper<>(Report.class),
                reportId);
    }

    /**
     * Fetch by test ID and test number
     * @param testId the test id
     * @param testNumber the test number
     * @return A list of records matching the test ID and test number
     * @throws DataNotFoundException if no records are found that match the query
     */
    public List<Report> fetch(int testId, int testNumber) throws DataNotFoundException {
        return runQueryMany("select * from report where aggregated = false and test_id = ? and test_number = ? ",
                new BeanPropertyRowMapper<>(Report.class),
                testId, testNumber);
    }

    /**
     * Fetch by test ID
     * @param testId the test ID
     * @return a list of records matching the test ID
     * @throws DataNotFoundException if no records are found that match the query
     */
    public List<Report> fetchByTestId(int testId) throws DataNotFoundException {
        return runQueryMany("select * from report where test_id = ?", new BeanPropertyRowMapper<>(Report.class),
                testId);
    }


    /**
     * The aggregated report record matching the input test ID and test number
     * @param testId the test ID
     * @param testNumber the test number
     * @return the record matching the report ID
     * @throws DataNotFoundException if no records are found that match the query
     */
    public Report fetchAggregated(int testId, int testNumber) throws DataNotFoundException {
        return runQuery(
                "select * from report where aggregated = true and test_id = ? and test_number = ?",
                new BeanPropertyRowMapper<>(Report.class),
                testId, testNumber);
    }


    /**
     * Collects the aggregation info for all the records. It can be used to determine which records
     * have and have not been aggregated
     * @return A list of aggregation information
     * @throws DataNotFoundException if no records are found that match the query
     */
    public List<ReportAggregationInfo> aggregationInfo() throws DataNotFoundException {
        return runQueryMany("SELECT test_id,test_number,sum(aggregated) AS aggregations FROM report " +
                "GROUP BY test_id,test_number ORDER BY test_id desc",
                new BeanPropertyRowMapper<>(ReportAggregationInfo.class));
    }

    public int getLastTestId() {
        try {
            Integer ret = runQuery("select MAX(test_id) from report",
                    new SingleColumnRowMapper<>(Integer.class));

            if (ret == null) {
                return -1;
            }

            return ret.intValue();
        }
        catch (DataNotFoundException e) {
            logger.warn("The query runner returned data not found. Returning -1 as the next test ID");

            return -1;
        }
    }

    public int getNextTestId() {
        return getLastTestId() + 1;
    }

    public int getLastTestNumber(int testId) {
        try {
            Integer ret = runQuery("select MAX(test_number) from report where test_id = ?",
                    new SingleColumnRowMapper<>(Integer.class),
                    testId);

            if (ret == null) {
                return -1;
            }

            return ret.intValue();
        }
        catch (DataNotFoundException e) {
            logger.warn("The query runner returned data not found. Returning -1 as the next test number");

            return -1;
        }
    }

    public int getNextTestNumber(int testId) {
        return getLastTestNumber(testId) + 1;
    }
}
