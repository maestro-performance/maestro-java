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
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.maestro.common.LogConfigurator;
import org.maestro.common.NetworkUtils;

import java.io.File;
import java.net.UnknownHostException;

public class ReportsTool {
    private CommandLine cmdLine;

    private String maestroUrl;
    private String host;
    private File dataDir;
    private boolean offline;


    static {
        LogConfigurator.defaultForDaemons();
    }


    public ReportsTool(final String[] args) {
        processCommand(args);

        initConfig();
    }

    protected void initConfig() {
        try {
            ConfigurationWrapper.initConfiguration(Constants.MAESTRO_CONFIG_DIR, "maestro-reports-tool.properties");
        } catch (Exception e) {
            System.err.println("Unable to initialize configuration file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
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
        options.addOption("m", "maestro-url", true,
                "maestro URL to connect to");
        options.addOption("H", "host", true,
                "optional hostname (to override auto-detection)");
        options.addOption("d", "data-dir", true, "Data directory");
        options.addOption("", "offline", false, "Run without connecting to a Maestro broker");

        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }

        offline = cmdLine.hasOption("offline");

        if (!offline) {
            maestroUrl = cmdLine.getOptionValue('m');
            if (maestroUrl == null) {
                System.err.println("Maestro URL is missing (option -m)");
                help(options, -1);
            }
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

    public CommandLine getCmdLine() {
        return cmdLine;
    }

    public String getMaestroUrl() {
        return maestroUrl;
    }

    public String getHost() {
        return host;
    }

    public File getDataDir() {
        return dataDir;
    }

    public boolean isOffline() {
        return offline;
    }

    protected int run(final ReportsToolLauncher launcher) {
        return launcher.launchServices();
    }

    protected int run() {
        return run(new DefaultToolLauncher(dataDir, offline, maestroUrl, host));
    }

    /**
     * Running this as a debug is something like:
     * java -m mqtt://maestro-broker:1883
     *      -d /storage/data
     */
    public static void main(String[] args) {
        ReportsTool reportsTool = new ReportsTool(args);

        System.exit(reportsTool.run());
    }


}
