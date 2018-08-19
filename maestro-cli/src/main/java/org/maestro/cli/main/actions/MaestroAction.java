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
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.MaestroResponse;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MaestroAction extends Action {
    private CommandLine cmdLine;

    private String maestroUrl;
    private String command;

    public MaestroAction(String[] args) {
        processCommand(args);
    }

    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("m", "maestro-url", true, "maestro URL to connect to");
        options.addOption("c", "command", true,
                "maestro command [ping, stats, halt, stop, unassign]");

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

        command = cmdLine.getOptionValue('c');
        if (command == null) {
            help(options, -1);
        }
    }

    public int run() {
        Maestro maestro;
        try {
            maestro = new Maestro(maestroUrl);

            CompletableFuture<List<? extends MaestroNote>> completableFuture;

            switch (command) {
                case "ping": {
                    completableFuture = maestro.pingRequest(MaestroTopics.PEER_TOPIC);
                    break;
                }
                case "stats": {
                    completableFuture = maestro.statsRequest();
                    break;
                }
                case "halt": {
                    completableFuture = maestro.halt();
                    break;
                }
                case "stop": {
                    completableFuture = maestro.stopAll();
                    break;
                }
                case "unassign": {
                    completableFuture = maestro.roleUnassign(MaestroTopics.PEER_TOPIC);
                    break;
                }
                default: {
                    System.err.println("Invalid command: " + command);
                    return 2;
                }
            }


            List<? extends MaestroNote> replies = completableFuture.get(5, TimeUnit.SECONDS);

            System.out.printf("%-20s    %-15s    %-30s    %-10s    %-10s%n",
                    "Command", "Name", "Host", "Group Name", "Member Name");

            for (MaestroNote note : replies) {
                if (note instanceof MaestroResponse) {
                    MaestroResponse response = (MaestroResponse) note;
                    PeerInfo peerInfo = response.getPeerInfo();

                    System.out.printf("%-20s    %-15s    %-30s    %-10s    %-10s%n",
                            response.getMaestroCommand(), peerInfo.peerName(), peerInfo.peerHost(),
                            peerInfo.groupInfo().groupName(), peerInfo.groupInfo().memberName());

                }


            }

            return 0;
        } catch (MaestroException | InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return 1;
    }
}
