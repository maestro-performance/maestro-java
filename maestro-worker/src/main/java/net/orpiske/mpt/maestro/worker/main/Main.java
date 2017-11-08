/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.orpiske.mpt.maestro.worker.main;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.common.Constants;
import net.orpiske.mpt.common.LogConfigurator;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.worker.base.MaestroWorkerManager;
import org.apache.commons.cli.*;

import java.io.IOException;


public class Main {
    private static CommandLine cmdLine;
    private static Options options;

    private static String maestroUrl;
    private static int port = 9200;

    /**
     * Prints the help for the action and exit
     * @param options the options object
     * @param code the exit code
     */
    private static void help(final Options options, int code) {
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp(Constants.BIN_NAME, options);
        System.exit(code);
    }

    private static void processCommand(String[] args) {
        CommandLineParser parser = new PosixParser();

        options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("m", "maestro-url", true,
                "maestro URL to connect to");
        options.addOption("p", "port", true,
                "port used to export metrics (default to 9200)");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        maestroUrl = cmdLine.getOptionValue('m');
        if (maestroUrl == null) {
            help(options, -1);
        }

        String portTmp = cmdLine.getOptionValue('p');
        if (portTmp != null) {
            port = Integer.parseInt(portTmp);
        }

    }

    public static void main(String[] args) {
        processCommand(args);

        LogConfigurator.debug();

        try {

            MaestroWorkerManager maestroWorkerManager = new MaestroWorkerManager(maestroUrl, "test");

            maestroWorkerManager.connect();
            maestroWorkerManager.subscribe(MaestroTopics.MAESTRO_SENDER_TOPICS);

            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (MaestroConnectionException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        System.exit(0);
    }
}
