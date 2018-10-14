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
 *
 */

package org.maestro.reports.server.main.actions;

import org.apache.commons.cli.*;
import org.maestro.common.LogConfigurator;
import org.maestro.reports.common.organizer.AggregatorOrganizer;
import org.maestro.reports.common.utils.ReportAggregator;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;
import org.maestro.reports.dto.ReportAggregationInfo;

import java.util.List;
import java.util.stream.Collectors;

public class AggregateAction extends Action {
    private ReportDao reportDao = new ReportDao();

    private CommandLine cmdLine;
    private String directory;
    private int testId;
    private int testNumber;
    private boolean all;

    public AggregateAction(String[] args) {
        processCommand(args);
    }

    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("d", "directory", true, "the directory to generate the report");
        options.addOption("t", "test-id", true, "the test id to aggregate");
        options.addOption("n", "test-number", true, "the test number to aggregate");
        options.addOption("", "all", false, "aggregate all records that haven't been aggregated yet");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        directory = cmdLine.getOptionValue('d');
        if (directory == null) {
            System.err.println("The output directory is a required option");
            help(options, 1);
        }

        String logLevel = cmdLine.getOptionValue('l');
        if (logLevel != null) {
            LogConfigurator.configureLogLevel(logLevel);
        }

        all = cmdLine.hasOption("all");

        if (!all) {
            String testIdStr = cmdLine.getOptionValue('t');
            if (testIdStr == null) {
                System.err.println("The test ID is a required option");
                help(options, 1);
            }

            try {
                testId = Integer.parseInt(testIdStr);
            }
            catch (Exception e) {
                System.err.println("The test ID must be a number");
                help(options, 1);
            }

            String testNumStr = cmdLine.getOptionValue('n');
            if (testNumStr == null) {
                System.err.println("The test number is a required option");
                help(options, 1);
            }

            try {
                testNumber = Integer.parseInt(testNumStr);
            } catch (Exception e) {
                System.err.println("The test number must be a number");
                help(options, 1);
            }
        }
    }

    private void aggregate(int iTestId, int iTestNumber) throws DataNotFoundException {
        AggregatorOrganizer organizer = new AggregatorOrganizer(directory);

        organizer.setTestId(iTestId);
        organizer.setTestNumber(iTestNumber);

        String aggregatedReportDir = organizer.organize(null);


        List<Report> reports = reportDao.fetch(iTestId, iTestNumber);

        List<String> reportDirs = reports.stream().map(Report::getLocation)
                .collect(Collectors.toList());

        new ReportAggregator(aggregatedReportDir).aggregate(reportDirs);
        Report aggregated = Report.aggregate(reports, aggregatedReportDir);

        reportDao.insert(aggregated);
    }

    public int run() {

        try {
            if (all) {
                List<ReportAggregationInfo> aggregationInfos = reportDao.aggregationInfo();

                for (ReportAggregationInfo aggregationInfo : aggregationInfos) {
                    if (aggregationInfo.getAggregations() == 0) {
                        System.out.println("Aggregating " + aggregationInfo.getTestId() + "/" +
                                aggregationInfo.getTestNumber());

                        aggregate(aggregationInfo.getTestId(), aggregationInfo.getTestNumber());
                    }
                }
            }
            else {
                aggregate(testId, testNumber);
            }


            return 0;
        } catch (DataNotFoundException e) {
            if (all) {
                System.err.println("Unable to aggregate the records: empty database");
            }
            else {
                System.err.println("Unable to aggregate the records: no records matching the given ID");
            }

            return 1;
        }

        catch (Exception e) {
            System.err.println("Unable to aggregate the reports: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}

