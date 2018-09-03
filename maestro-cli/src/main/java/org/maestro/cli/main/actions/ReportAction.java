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

package org.maestro.cli.main.actions;

import org.apache.commons.cli.*;
import org.maestro.common.LogConfigurator;
import org.maestro.reports.ReportGenerator;
import org.maestro.reports.composed.ComposedIndexGenerator;
import org.maestro.reports.context.NodeReportContext;
import org.maestro.reports.context.ReportContext;
import org.maestro.reports.context.common.NodePropertyContext;
import org.maestro.reports.context.common.WithWarmUpContext;
import org.maestro.reports.processors.DiskCleaner;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReportAction extends Action {
    private CommandLine cmdLine;

    private String directory;
    private boolean clean;
    private final Map<String,String> indexProperties = new HashMap<>();
    private boolean composed;
    private boolean withWarmUp = false;
    private String sutNodeProperties;


    public ReportAction(String[] args) {
        processCommand(args);
    }


    private void addProperty(final String str) {
        String[] tmp = str.split("=");

        indexProperties.put(tmp[0], tmp[1]);
    }

    private void parseProperties(final String str) {
        if (str == null) {
            return;
        }

        String[] array = str.split(",");

        Arrays.asList(array).forEach(this::addProperty);
    }

    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("d", "directory", true, "the directory to generate the report");
        options.addOption("l", "log-level", true, "the log level to use [trace, debug, info, warn]");
        options.addOption("C", "clean", false, "clean the report directory after processing");
        options.addOption("", "with-properties", true, "pass optional properties (ie.: saved along with the index)");
        options.addOption("", "composed", false, "generate the composed index");
        options.addOption("", "with-warm-up", false, "consider the first test iteration as warm-up");
        options.addOption("", "with-sut-node-properties-from", true,
                "SUT node properties as provided in the properties file");

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

        clean = cmdLine.hasOption('C');
        parseProperties(cmdLine.getOptionValue("with-properties"));
        composed = cmdLine.hasOption("composed");
        withWarmUp = cmdLine.hasOption("with-warm-up");
        sutNodeProperties = cmdLine.getOptionValue("with-sut-node-properties-from");
    }

    public int run() {
        try {
            if (composed) {
                ComposedIndexGenerator.generate(new File(directory));
            }
            else {
                ReportGenerator reportGenerator = new ReportGenerator(directory);

                if (clean) {
                    reportGenerator.getPostProcessors().add(new DiskCleaner());
                }

                reportGenerator.setIndexProperties(indexProperties);

                ReportContext reportContext = null;

                if (withWarmUp) {
                    reportContext = new WithWarmUpContext();
                }

                NodeReportContext nodeReportContext = null;
                if (sutNodeProperties != null) {
                    nodeReportContext = new NodePropertyContext(sutNodeProperties);
                }

                reportGenerator.generate(reportContext, nodeReportContext);

                System.out.println("Report generated successfully");
            }
            return 0;
        }
        catch (Exception e) {
            System.err.println("Unable to generate the performance test reports");
            e.printStackTrace();
            return 1;
        }
    }
}
