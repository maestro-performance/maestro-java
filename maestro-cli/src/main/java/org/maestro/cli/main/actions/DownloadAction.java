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
import org.maestro.client.exchange.support.GroupInfo;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.GetResponse;
import org.maestro.common.LogConfigurator;
import org.maestro.common.Role;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.reports.downloaders.DefaultDownloader;
import org.maestro.reports.InspectorReportResolver;
import org.maestro.reports.InterconnectInspectorReportResolver;
import org.maestro.reports.downloaders.ReportsDownloader;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DownloadAction extends Action {
    private static class Server {
        PeerInfo peerInfo;
        String address;
    }

    private CommandLine cmdLine;

    private String maestroUrl;
    private String directory;
    private List<Server> servers;
    private String result;
    private int from;
    private int to;
    private String customResolver;

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
        options.addOption("", "with-inspector-resolver", true, "add custom resolver");

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

        String serverList = cmdLine.getOptionValue('s');
        if (serverList == null) {
            maestroUrl = cmdLine.getOptionValue('m');
            if (maestroUrl == null) {
                System.err.println("Either the a server list specified via 'servers' option or a maestro " +
                    "URL (for dynamic resolution) are required");
                help(options, 1);
            }
        }
        else {
            servers = new LinkedList<>();
            // ie.: sender@host:4423,receiver@host:4421
            String[] items = serverList.split(",");
            for (String item : items) {
                Server server = new Server();
                String[] serverString = item.split("@");

                server.peerInfo = new PeerInfo() {
                    @Override
                    public void setRole(Role role) {

                    }

                    @Override
                    public Role getRole() {
                        return Role.hostTypeByName(serverString[0]);
                    }

                    @Override
                    public String peerName() {
                        return serverString[0];
                    }

                    @Override
                    public String peerHost() {
                        return serverString[1];
                    }

                    @Override
                    public GroupInfo groupInfo() {
                        return null;
                    }
                };
                server.address = serverString[1];

                servers.add(server);
            }
        }

        result = cmdLine.getOptionValue('r');
        if (result == null) {
            result = "success";
        }

        String logLevel = cmdLine.getOptionValue('l');
        if (logLevel != null) {
            LogConfigurator.configureLogLevel(logLevel);
        }

        customResolver = cmdLine.getOptionValue("with-inspector-resolver");
    }

    public int run() {
        try {
            resolveDataServers();

            for (Server server : servers) {
                ReportsDownloader rd = new DefaultDownloader(directory);

                if (customResolver.equals("InterconnectInspector")) {
                    rd.addReportResolver(Role.INSPECTOR, new InterconnectInspectorReportResolver());
                }
                else {
                    if (customResolver.equals("ArtemisInspector")) {
                        rd.addReportResolver(Role.INSPECTOR, new InspectorReportResolver());
                    }
                }

                rd.getOrganizer().setResultType(result);

                int i = from;
                do {
                    String resourcePath = Integer.toString(i);
                    System.out.println("Downloading reports from http://" + server + "/" + resourcePath);

                    rd.getOrganizer().getTracker().setCurrentTest(i);
                    rd.downloadAny(server.peerInfo, resourcePath +"/");
                    i++;
                } while (i <= to);
            }


            return 0;
        }
        catch (Exception e) {
            System.err.println("Unable to download the performance test reports");
            e.printStackTrace();
            return 1;
        }
    }

    private void resolveDataServers() throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Resolving data servers");

        if (servers == null) {
            servers = new LinkedList<>();
            Maestro maestro = new Maestro(maestroUrl);

            System.out.println("Sending the request");
            List<? extends MaestroNote> replies = maestro
                    .getDataServer()
                    .get(10, TimeUnit.SECONDS);

            for (MaestroNote note : replies) {
                GetResponse reply = (GetResponse) note;
                Server server = new Server();

                server.peerInfo = reply.getPeerInfo();
                server.address = reply.getValue();

                servers.add(server);
            }
        }
    }
}
