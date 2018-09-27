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
import org.maestro.common.Constants;
import org.maestro.reports.server.loader.ReportDirectoryWalker;

import java.io.File;

public class LoadAction extends Action {
    private static CommandLine cmdLine;

    private static File dataDir;

    public LoadAction(String[] args) {
        processCommand(args);
    }

    /**
     * Prints the help for the action and exit
     * @param options the options object
     * @param code the exit code
     */
    protected void help(final Options options, int code) {
        HelpFormatter formatter = new HelpFormatter();

        System.out.println("maestro " + Constants.VERSION + "\n");
        formatter.printHelp(Constants.BIN_NAME, options);
        System.exit(code);
    }

    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("d", "data-dir", true, "Data directory");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        String dataDirVal = cmdLine.getOptionValue('d');
        if (dataDirVal == null) {
            System.err.println("The data directory is missing (option -d)");

            help(options, -1);
        }
        dataDir = new File(dataDirVal);
    }

    private static int loadData() {
        ReportDirectoryWalker walker = new ReportDirectoryWalker();

        try {
            walker.load(dataDir);
            return 0;
        }
        catch (Throwable t) {
            return 1;
        }
    }

    public int run() {
        try {
            return loadData();
        }
        catch (Exception e) {
            System.err.println("Unable to generate the performance test reports");
            e.printStackTrace();
            return 1;
        }
    }
}