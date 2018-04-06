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

import org.maestro.cli.data.rate.RateToHistogram;
import org.apache.commons.cli.*;

import java.io.*;

public class DataAction extends Action {
    private CommandLine cmdLine;

    private String input;
    private String output;

    public DataAction(String[] args) {
        processCommand(args);
    }

    @Override
    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("a", "action", true, "the action to execute [rate-to-histogram]");
        options.addOption("i", "input", true, "the input filename");
        options.addOption("o", "output", true, "the output filename (if none, will print to stdout)");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        input = cmdLine.getOptionValue('i');
        if (input == null) {
            System.err.println("The input filename is a required option");
            help(options, 1);
        }

        output = cmdLine.getOptionValue('o');
    }

    @Override
    public int run() {
        PrintStream ps = System.out;
        if (output != null) {
            File outputFile = new File(output);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                return 1;
            }

            ps = new PrintStream(fos);
        }

        try {
            RateToHistogram.convert(input, ps);

            return 0;
        } catch (IOException e) {
            System.err.println("Error converting rate to histogram: " + e.getMessage());

            e.printStackTrace();
            return 1;
        }
        finally {
            ps.close();
        }
    }
}
