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

import org.maestro.common.exceptions.MaestroException;
import org.maestro.client.Maestro;
import org.maestro.common.client.notes.MaestroNote;
import org.apache.commons.cli.*;

import java.util.List;

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
        options.addOption("c", "command", true, "maestro command [ping, flush, stats, stop]");

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

            switch (command) {
                case "ping": {
                    maestro.pingRequest();
                    break;
                }
                case "flush": {
                    maestro.flushRequest();
                    break;
                }
                case "stats": {
                    maestro.statsRequest();
                    break;
                }
                case "halt": {
                    maestro.halt();
                    break;
                }
                case "start-inspector": {
                    maestro.startInspector();
                    break;
                }
                case "stop": {
                    maestro.stopSender();
                    maestro.stopReceiver();
                    maestro.stopInspector();
                    break;
                }
            }

            List<MaestroNote> replies = maestro.collect(1000, 10);

            for (MaestroNote note : replies) {
                System.out.println("Reply: " + note);
            }

            return 0;
        } catch (MaestroException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return 1;
    }
}
