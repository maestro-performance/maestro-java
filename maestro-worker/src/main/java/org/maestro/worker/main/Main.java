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

package org.maestro.worker.main;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.maestro.common.LogConfigurator;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.client.exchange.MaestroTopics;
import org.apache.commons.cli.*;

import java.io.File;


public class Main {
    private static CommandLine cmdLine;

    private static String maestroUrl;
    private static String worker;
    private static String role;
    private static String host;
    private static String logDir;

    /**
     * Prints the help for the action and exit
     * @param options the options object
     * @param code the exit code
     */
    private static void help(final Options options, int code) {
        HelpFormatter formatter = new HelpFormatter();

        System.out.println("maestro " + Constants.VERSION + "\n");
        formatter.printHelp(Constants.BIN_NAME, options);
        System.exit(code);
    }

    private static void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("m", "maestro-url", true,
                "maestro URL to connect to");
        options.addOption("w", "worker", true,
                "maestro worker to use");
        options.addOption("r", "role", true,
                "worker role (sender or receiver)");
        options.addOption("H", "host", true,
                "this' host hostname");
        options.addOption("l", "log-dir", true,
                "this' host hostname");

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
            System.err.println("Maestro URL is missing (option -m)");
            help(options, -1);
        }

        worker = cmdLine.getOptionValue('w');
        if (worker == null) {
            System.err.println("The worker class is missing (option -w)");
            help(options, -1);
        }

        role = cmdLine.getOptionValue('r');
        if (role == null) {
            System.err.println("The worker role is missing (option -r)");
            help(options, -1);
        }
        System.setProperty("maestro.worker.role", role);

        host = cmdLine.getOptionValue('H');
        if (host == null) {
            System.err.println("The peer hostname is missing (option -H)");
            help(options, -1);
        }

        logDir = cmdLine.getOptionValue('l');
        if (logDir == null) {
            System.err.println("The log directory is missing (option -l)");

            help(options, -1);
        }
    }

    /**
     * Running this as a debug is something like:
     * java -Djava.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory {class}
     *      -m mqtt://maestro-broker:1883
     *      -r sender
     *      -H localhost
     *      -w org.maestro.mpt.maestro.worker.jms.JMSSenderWorker
     *      -l /storage/tmp/maestro-java/sender
     * @param args
     */
    public static void main(String[] args) {
        processCommand(args);

        LogConfigurator.defaultForDaemons();
        try {
            ConfigurationWrapper.initConfiguration(Constants.MAESTRO_CONFIG_DIR, "maestro-worker.properties");
        } catch (Exception e) {
            System.err.println("Unable to initialize configuration file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        try {
            Class<MaestroWorker> clazz = (Class<MaestroWorker>) Class.forName(worker);

            MaestroWorkerExecutor executor = new MaestroWorkerExecutor(maestroUrl, role, host, new File(logDir), clazz);

            switch (role) {
                case "sender": {
                    executor.start(MaestroTopics.MAESTRO_SENDER_TOPICS);
                    executor.run();
                    break;
                }
                case "receiver": {
                    executor.start(MaestroTopics.MAESTRO_RECEIVER_TOPICS);
                    executor.run();
                    break;
                }
                case "inspector": {
                    executor.start(MaestroTopics.MAESTRO_INSPECTOR_TOPICS);
                    executor.run();
                    break;
                }
                default: {
                    System.err.println("Invalid role name: " + role);
                    System.exit(1);
                    break;
                }
            }

            System.out.println("Finished execution ...");
        } catch (MaestroException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}
