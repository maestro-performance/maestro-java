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
import org.maestro.cli.main.actions.exec.GroovyWrapper;

import java.io.File;
import java.io.IOException;

public class ExecAction extends Action {
    private CommandLine cmdLine;

    private File script;
    private String directory;

    public ExecAction(final String[] args) {
        processCommand(args);
    }

    @Override
    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("d", "directory", true, "the directory to generate the report");
        options.addOption("s", "script", true, "the path to the test script");

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

        String fileStr = cmdLine.getOptionValue('s');
        if (fileStr == null) {
            System.err.println("The test script is required option");
            help(options, 1);
        }

        script = new File(fileStr);
    }

    @Override
    public int run() {
        GroovyWrapper groovyWrapper = new GroovyWrapper();

        try {
            groovyWrapper.run(script, new String[] {directory});
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }
}
