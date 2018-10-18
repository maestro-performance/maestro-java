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
import org.maestro.common.LogConfigurator;

abstract class ManageAction extends Action {
    private CommandLine cmdLine;
    protected int testId;
    protected String comments;

    public ManageAction(String[] args) {
        processCommand(args);
    }

    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("t", "test-id", true, "the test id to manage");
        options.addOption("", "comments", true, "test comments");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
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

        comments = cmdLine.getOptionValue("comments");
    }
}
