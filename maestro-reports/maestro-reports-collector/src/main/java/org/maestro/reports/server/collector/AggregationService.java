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

package org.maestro.reports.server.collector;

import org.maestro.common.client.notes.TestExecutionInfo;
import org.maestro.reports.common.organizer.AggregatorOrganizer;
import org.maestro.reports.common.utils.ReportAggregator;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;
import org.maestro.reports.dto.ReportAggregationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AggregationService {
    private static final Logger logger = LoggerFactory.getLogger(AggregationService.class);
    private final ReportDao dao = new ReportDao();
    private final List<PostAggregationHook> hooks = new LinkedList<>();

    private final String directory;
    private final TestExecutionInfo testExecutionInfo;

    public AggregationService(final String directory, final TestExecutionInfo testExecutionInfo) {
        this.directory = directory;
        this.testExecutionInfo = testExecutionInfo;
    }

    public List<PostAggregationHook> getHooks() {
        return hooks;
    }

    private void runAggregate(int iTestId, int iTestNumber) throws DataNotFoundException {
        AggregatorOrganizer organizer = new AggregatorOrganizer(directory);

        organizer.setTestId(iTestId);
        organizer.setTestNumber(iTestNumber);

        String aggregatedReportDir = organizer.organize(null);

        List<Report> reports = dao.fetch(iTestId, iTestNumber);

        List<String> reportDirs = reports.stream().map(Report::getLocation)
                .collect(Collectors.toList());

        new ReportAggregator(aggregatedReportDir).aggregate(reportDirs);
        Report aggregated = Report.aggregate(reports, aggregatedReportDir);

        dao.insert(aggregated);

        logger.debug("Running the post-aggregation hooks");
        hooks.forEach(hook -> hook.exec(testExecutionInfo, reports));
    }


    public void aggregate(int maxTestId, int maxTestNumber) {
        List<ReportAggregationInfo> aggregationInfos;
        try {
            aggregationInfos = dao.aggregationInfo();
        } catch (DataNotFoundException e) {
            logger.error("Unable to aggregate data records: {}", e.getMessage());

            return;
        }

        for (ReportAggregationInfo aggregationInfo : aggregationInfos) {
            if (aggregationInfo.getAggregations() == 0) {
                if (isBelowFilter(maxTestId, maxTestNumber, aggregationInfo)) {
                    logger.info("Aggregating {}/{}", aggregationInfo.getTestId(), aggregationInfo.getTestNumber());

                    try {
                        runAggregate(aggregationInfo.getTestId(), aggregationInfo.getTestNumber());
                    } catch (DataNotFoundException e) {
                        logger.debug("No records to aggregate");
                    }
                }
                else {
                    logger.debug("Temporarily ignoring test {}/{} for aggregation", aggregationInfo.getTestId(),
                            aggregationInfo.getTestNumber());
                }
            }
        }
    }

    private boolean isBelowFilter(int maxTestId, int maxTestNumber, ReportAggregationInfo aggregationInfo) {
        return aggregationInfo.getTestId() < maxTestId ||
                (aggregationInfo.getTestId() == maxTestId && aggregationInfo.getTestNumber() <= maxTestNumber);
    }
}
