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
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;

import java.io.File;
import java.io.IOException;

public class ExtensionPointAction extends Action {
    private static final String REPOSITORY_URL = "https://github.com/maestro-performance/maestro-agent-sample-extpoints.git";
    private CommandLine cmdLine;
    private String directory;
    private String name;

    public ExtensionPointAction(final String[] args) {
        processCommand(args);
    }

    @Override
    protected void processCommand(final String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("d", "directory", true, "the directory to contain the extension points");
        options.addOption("n", "name", true, "the project name");

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
            System.err.println("The repository directory is a required option");
            help(options, 1);
        }

        name = cmdLine.getOptionValue('n');
        if (name == null) {
            System.err.println("The project name is a required option");
            help(options, 1);
        }
    }

    private void doClone() throws GitAPIException, IOException {
        CloneCommand cloneCommand = Git.cloneRepository();

        cloneCommand.setURI(REPOSITORY_URL);

        File repositoryDir = new File(directory, name);
        cloneCommand.setDirectory(repositoryDir);
        cloneCommand.setProgressMonitor(new TextProgressMonitor());

        cloneCommand.call();
        System.out.println("Project directory for project created at " + repositoryDir);

        FileUtils.deleteDirectory(new File(repositoryDir, ".git"));
    }

    @Override
    public int run() {
        try {
            doClone();

            return 0;
        }
        catch (Exception e) {
            System.err.println("Unable to create a new project");
            e.printStackTrace();
            return 1;
        }
    }
}
