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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.maestro.common.HostTypes;
import org.maestro.common.ResultStrings;
import org.maestro.reports.dao.builder.TestDatabaseBuilder;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;
import org.maestro.reports.dto.ReportAggregationInfo;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class ReportDaoTest {
    private ReportDao dao;

    @Before
    public void setUp() throws Exception {
        dao = new ReportDao(new TestDatabaseBuilder("testInsert"));
    }

    @Test
    public void testInsert() {
        Report report = new Report();

        report.setTestHost("localhost");
        report.setTestHostRole(HostTypes.SENDER_HOST_TYPE);
        report.setTestName("unit-test");
        report.setTestNumber(1);
        report.setTestId(1);
        report.setLocation(this.getClass().getResource(".").getPath());
        report.setTestResult(ResultStrings.SUCCESS);
        report.setTestScript("undefined");

        int ret = dao.insert(report);
        assertTrue("The record does not match the expected value", ret >= 9);
    }

    @Test
    public void testFetchAll() throws DataNotFoundException {
        List<Report> reports = dao.fetch();

        assertTrue("The database should not be empty", reports.size() > 0);

        long aggregatedCount = reports.stream().filter(r -> r.isAggregated()).count();
        long expectedAggCount = 0;
        assertEquals("Aggregated reports should not be displayed by default", expectedAggCount, aggregatedCount);
    }

    @Test
    public void testFetchById() throws DataNotFoundException {
        List<Report> reports = dao.fetch(2, 1);

        assertTrue("There should be at least 3 records for the given ID", reports.size() > 3);

        long aggregatedCount = reports.stream().filter(r -> r.isAggregated()).count();
        long expectedAggCount = 0;
        assertEquals("Aggregated reports should not be displayed by default", expectedAggCount, aggregatedCount);
    }


    @Test
    public void testAggregationInfo() throws DataNotFoundException {
        List<ReportAggregationInfo> aggregationInfos = dao.aggregationInfo();

        long expectedAggregatedCount = 2;
        assertEquals("Unexpected amount of aggregated records", expectedAggregatedCount, aggregationInfos.size());
    }
}
