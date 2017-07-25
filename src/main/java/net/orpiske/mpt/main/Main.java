/*
 *  Copyright ${YEAR} ${USER}
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

package net.orpiske.mpt.main;

import net.orpiske.mpt.utils.Constants;
import org.apache.commons.cli.*;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class Main {
    private static CommandLine cmdLine;
    private static Options options;

    private static String url;

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
        options.addOption("m", "maestro-url", true, "maestro URL to connect to");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        url = cmdLine.getOptionValue('m');
        if (url == null) {
            help(options, -1);
        }
    }

    public static void main(String[] args) {
        processCommand(args);

        Maestro maestro = null;
        try {
            maestro = new Maestro(url);

            maestro.pingRequest();

            System.exit(0);
        } catch (MqttException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        System.exit(1);
    }



}
