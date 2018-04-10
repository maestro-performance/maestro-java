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

import org.maestro.reports.ReportsDownloader;
import org.apache.commons.cli.*;

public class DownloadAction extends Action {
    private CommandLine cmdLine;

    private String directory;
    private String[] servers;
    private String result;
    private int from;
    private int to;

    public DownloadAction(String[] args) {
        processCommand(args);
    }

    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("d", "directory", true, "the directory to generate the report");
        options.addOption("l", "log-level", true, "the log level to use [trace, debug, info, warn]");
        options.addOption("f", "from-test", true, "the initial test execution number");
        options.addOption("t", "to-test", true, "the final test execution number");
        options.addOption("s", "servers", true, "a command separated list of servers");
        options.addOption("r", "result", true, "the result to assign to the downloaded files [success,failed]");


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

        try {
            from = Integer.parseInt(cmdLine.getOptionValue('f'));
            to = Integer.parseInt(cmdLine.getOptionValue('t'));
        }
        catch (Exception e) {
            help(options, 1);
        }


        if (to < from) {
            System.err.println("The 'from' must not be smaller than 'to'");
            help(options, 1);
        }

        String serverList = cmdLine.getOptionValue('s');
        if (serverList == null) {
            System.err.println("The 'servers' option is required");
            help(options, 1);
        }

        assert serverList != null;
        servers = serverList.split(",");

        result = cmdLine.getOptionValue('r');
        if (result == null) {
            result = "success";
        }

        String logLevel = cmdLine.getOptionValue('l');
        if (logLevel != null) {
            configureLogLevel(logLevel);
        }
    }

    public int run() {
        try {
            for (String server : servers) {
                ReportsDownloader rd = new ReportsDownloader(directory);

                rd.getOrganizer().setResultType(result);
                int i = from;
                do {
                    String resourcePath = Integer.toString(i);
                    System.out.println("Downloading reports from http://" + server + "/" + resourcePath);

                    rd.getOrganizer().getTracker().setCurrentTest(i);
                    rd.downloadAny(server, resourcePath +"/");
                    i++;
                } while (i <= to);
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
