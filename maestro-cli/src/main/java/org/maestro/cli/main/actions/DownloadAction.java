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
import org.maestro.client.Maestro;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.exchange.support.PeerSet;
import org.maestro.common.LogConfigurator;
import org.maestro.reports.downloaders.BrokerDownloader;
import org.maestro.reports.downloaders.ReportsDownloader;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DownloadAction extends Action {
    private CommandLine cmdLine;

    private String maestroUrl;
    private String directory;
    private List<String> servers;
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
        options.addOption("s", "servers", true, "a command separated list of servers (ie: sender@host0:port,receiver@host1:port)");
        options.addOption("r", "result", true, "the result to assign to the downloaded files [success,failed]");
        options.addOption("m", "maestro-url", true, "maestro URL to connect to");

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

        maestroUrl = cmdLine.getOptionValue('m');
        if (maestroUrl == null) {
            System.err.println("The maestro path is required");
            help(options, 1);
        }

        String serverList = cmdLine.getOptionValue('s');
        if (serverList != null) {
            servers = new LinkedList<>();

            String[] items = serverList.split(",");
            servers = Arrays.asList(items);
        }

        result = cmdLine.getOptionValue('r');
        if (result == null) {
            result = "success";
        }

        LogConfigurator.configureLogLevel(cmdLine.getOptionValue('l'));
    }

    public int run() {
        try {

            Maestro maestro = new Maestro(maestroUrl);
            ReportsDownloader rd = new BrokerDownloader(maestro, directory);

            PeerSet peerSet = maestro.getPeers();

            rd.getOrganizer().setResultType(result);

            for (String server : servers) {
                peerSet.getPeers().forEach((k,v) -> download(rd, server, k, v));
            }

            return 0;
        }
        catch (Exception e) {
            System.err.println("Unable to download the performance test reports");
            e.printStackTrace();
            return 1;
        }
    }

    private void download(final ReportsDownloader rd, final String host, final String id, final PeerInfo peerInfo) {
        if (!peerInfo.peerHost().equals(host)) {
            return;
        }

        System.out.println("Downloading reports from " + peerInfo.prettyName());

        int i = from;
        do {
            String resourcePath = Integer.toString(i);

            rd.getOrganizer().getTracker().setCurrentTest(i);

            rd.downloadAny(id, resourcePath);

            rd.waitForComplete();

            i++;
        } while (i <= to);

    }
}
