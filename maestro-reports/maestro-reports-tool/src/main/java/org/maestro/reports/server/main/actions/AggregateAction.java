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
import org.maestro.reports.common.organizer.Organizer;
import org.maestro.reports.common.utils.ReportAggregator;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dto.Report;

import java.util.List;
import java.util.stream.Collectors;

public class AggregateAction extends Action {
    private CommandLine cmdLine;
    private String directory;
    private int testId;
    private int testNumber;

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
            System.err.println("The input directory is a required option");
            help(options, 1);
        }

        String logLevel = cmdLine.getOptionValue('l');
        if (logLevel != null) {
            LogConfigurator.configureLogLevel(logLevel);
        }

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
        }
        catch (Exception e) {
            System.err.println("The test number must be a number");
            help(options, 1);
        }
    }

    public int run() {
        AggregatorOrganizer organizer = new AggregatorOrganizer(directory);

        organizer.setTestId(testId);
        organizer.setTestNumber(testNumber);

        try {
            String aggregatedReportDir = organizer.organize(null);

            ReportDao reportDao = new ReportDao();
            List<Report> reports = reportDao.fetch(testId, testNumber);

            List<String> reportDirs = reports.stream().map(Report::getLocation)
                    .collect(Collectors.toList());

            new ReportAggregator(aggregatedReportDir).aggregate(reportDirs);
            Report aggregated = Report.aggregate(reports, aggregatedReportDir);

            reportDao.insert(aggregated);

            return 0;
        } catch (Exception e) {
            System.err.println("Unable to aggregate the performance test reports");
            e.printStackTrace();
            return 1;
        }
    }
}

