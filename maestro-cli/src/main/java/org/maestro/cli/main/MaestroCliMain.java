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

package org.maestro.cli.main;

import org.maestro.cli.main.actions.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.maestro.common.LogConfigurator;

import static java.util.Arrays.copyOfRange;

public class MaestroCliMain {
    static {
        LogConfigurator.defaultForDaemons();
    }

    /**
     * Prints the help for the action and exit
     * @param code the exit code
     */
    private static void help(int code) {
        System.out.println("maestro " + Constants.VERSION + "\n");
        System.out.println("Usage: maestro-cli <action>\n");

        System.out.println("Actions:");
        System.out.println("   download");
        System.out.println("   report");
        System.out.println("   extension-point");
        System.out.println("   maestro");
        System.out.println("   exec");
        System.out.println("   aggregate");
        System.out.println("----------");
        System.out.println("   help");
        System.out.println("   --version");

        System.exit(code);
    }

    public static void main(String[] args) {
        try {
            ConfigurationWrapper.initConfiguration(Constants.MAESTRO_CONFIG_DIR, "maestro-cli.properties");
        } catch (Exception e) {
            System.err.println("Unable to initialize configuration file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        if (args.length == 0) {
            System.err.println("The action is missing!");
            help(1);
        }
        else {
            System.out.println("Running " + args[0]);
        }

        String first = args[0];
        String[] newArgs = copyOfRange(args, 1, args.length);

        if (first.equals("help")) {
            help(1);
        }

        Action action;
        switch (first) {
            case "report": {
                action = new ReportAction(newArgs);
                break;
            }
            case "maestro": {
                action = new MaestroAction(newArgs);
                break;
            }
            case "download": {
                action = new DownloadAction(newArgs);
                break;
            }
            case "extension-point": {
                action = new ExtensionPointAction(newArgs);
                break;
            }
            case "exec": {
                action = new ExecAction(newArgs);
                break;
            }
            case "aggregate": {
                action = new AggregateAction(newArgs);
                break;
            }
            default: {
                help(1);
                return;
            }
        }

        System.exit(action.run());
    }



}
