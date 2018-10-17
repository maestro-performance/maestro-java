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

import org.maestro.reports.common.organizer.AggregatorOrganizer;
import org.maestro.reports.common.utils.ReportAggregator;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;
import org.maestro.reports.dto.ReportAggregationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AggregationService {
    private static final Logger logger = LoggerFactory.getLogger(AggregationService.class);
    private final String directory;

    private ReportDao dao = new ReportDao();

    public AggregationService(String directory) {
        this.directory = directory;
    }

    private void aggregate(int iTestId, int iTestNumber) throws DataNotFoundException {
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
    }


    public void aggregate() {
        List<ReportAggregationInfo> aggregationInfos = null;
        try {
            aggregationInfos = dao.aggregationInfo();
        } catch (DataNotFoundException e) {
            logger.error("Unable to aggregate data records: {}", e.getMessage());

            return;
        }

        for (ReportAggregationInfo aggregationInfo : aggregationInfos) {
            if (aggregationInfo.getAggregations() == 0) {
                logger.info("Aggregating {}/{}", aggregationInfo.getTestId(), aggregationInfo.getTestNumber());

                try {
                    aggregate(aggregationInfo.getTestId(), aggregationInfo.getTestNumber());
                } catch (DataNotFoundException e) {
                    logger.debug("No records to aggregate");
                }
            }
        }
    }
}
