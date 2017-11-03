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

package net.orpiske.mpt.exporter.main;

import java.io.IOException;

import org.apache.commons.cli.*;
import org.eclipse.paho.client.mqttv3.MqttException;

import net.orpiske.mpt.utils.*;
import utils.LogConfigurator;


public class Main {
    private static CommandLine cmdLine;
    private static Options options;

    private static String maestroUrl;
    private static String exportUrl;

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
        options.addOption("m", "maestro-maestroUrl", true,
                "maestro URL to connect to");
        options.addOption("e", "export-maestroUrl", true,
                "export URL where the data will be available");

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

        exportUrl = cmdLine.getOptionValue('e');
        if (exportUrl == null) {
            help(options, -1);
        }
    }

    public static void main(String[] args) {
        processCommand(args);

        LogConfigurator.debug();

        try {
            MaestroExporter exporter = new MaestroExporter(maestroUrl, exportUrl);

            exporter.run();

        } catch (MqttException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        System.exit(0);
    }
}
