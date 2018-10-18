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

package org.maestro.reports.server.main.actions;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.maestro.common.LogConfigurator;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ConsolidateAction extends Action {
    private ReportDao reportDao = new ReportDao();

    private CommandLine cmdLine;
    private String directory;

    public ConsolidateAction(String[] args) {
        processCommand(args);
    }

    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("d", "directory", true, "the directory to put the consolidated reports");

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
    }

    public int run() {

        try {
            List<Report> reports = reportDao.fetchAll();
            ReportOrganizer organizer = new ReportOrganizer(directory);

            for (Report report : reports) {
                File reportDir = new File(report.getLocation());

                if (reportDir.exists()) {
                    if (reportDir.isDirectory()) {
                        String newDirectory = organizer.organize(report);
                        System.out.println("Moving record from " + reportDir + " to " + newDirectory);

                        FileUtils.copyDirectory(reportDir, new File(newDirectory));
                        report.setLocation(newDirectory);

                        reportDao.update(report);
                    }
                }
            }

            return 0;
        } catch (DataNotFoundException e) {
            return 1;
        }

        catch (Exception e) {
            System.err.println("Unable to consolidate the reports: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
