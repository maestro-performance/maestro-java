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

package org.maestro.reports.server.main;

import org.apache.commons.cli.*;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.maestro.common.LogConfigurator;
import org.maestro.common.NetworkUtils;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.data.server.http.HttpDataServer;
import org.maestro.reports.server.collector.DefaultReportsCollector;
import org.maestro.worker.common.ds.MaestroDataServer;
import org.maestro.worker.common.executor.MaestroWorkerExecutor;

import java.io.File;
import java.net.UnknownHostException;

public class ReportsServer {
    private static CommandLine cmdLine;

    private static String maestroUrl;
    private static String host;
    private static File dataDir;

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
        options.addOption("H", "host", true,
                "optional hostname (to override auto-detection)");
        options.addOption("d", "data-dir", true, "Data directory");

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


        host = cmdLine.getOptionValue('H');
        if (host == null) {
            try {
                host = NetworkUtils.getHost("maestro.worker.host");
            } catch (UnknownHostException e) {
                System.err.println("Unable to determine the hostname and the peer hostname is missing (set with option -H)");
                help(options, -1);
            }
        }

        String dataDirVal = cmdLine.getOptionValue('d');
        if (dataDirVal == null) {
            System.err.println("The data directory is missing (option -d)");

            help(options, -1);
        }
        dataDir = new File(dataDirVal);
    }

    /**
     * Running this as a debug is something like:
     * java -m mqtt://maestro-broker:1883
     *      -d /storage/data
     */
    public static void main(String[] args) {
        try {
            ConfigurationWrapper.initConfiguration(Constants.MAESTRO_CONFIG_DIR, "maestro-reports-server.properties");
        } catch (Exception e) {
            System.err.println("Unable to initialize configuration file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        processCommand(args);

        LogConfigurator.defaultForDaemons();

        try {
            MaestroDataServer dataServer = new HttpDataServer(dataDir, host);
            MaestroWorkerExecutor executor;

            final PeerInfo peerInfo = new ReportsServerPeer(host);

            DefaultReportsCollector maestroPeer = new DefaultReportsCollector(maestroUrl, peerInfo, dataDir, dataServer);
            executor = new MaestroWorkerExecutor(maestroPeer, dataServer);

            String[] topics = MaestroTopics.collectorTopics(maestroPeer.getId(), peerInfo);

            executor.start(topics, 10, 1000);
            executor.run();

            System.out.println("Finished execution ...");
        } catch (MaestroException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        System.exit(0);
    }
}
