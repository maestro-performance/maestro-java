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

package org.maestro.agent.base;

import org.maestro.common.URLQuery;
import org.maestro.common.agent.AgentEndpoint;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.Test;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.common.worker.WorkerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;

/**
 * Abstract handler class for external points scripts
 */
public abstract class AbstractHandler implements AgentEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

    private MaestroClient client;
    private MaestroNote note;
    private WorkerOptions workerOptions;
    private Test currentTest;

    /**
     * Sets the content of the note associated with the external endpoint
     * @param note the note
     */
    public void setMaestroNote(final MaestroNote note) {
        this.note = note;
    }

    /**
     * Get maestro note
     * @return MaestroNote
     */
    public MaestroNote getNote() {
        return note;
    }

    /**
     * Sets the content of the client associated with the external endpoint.
     * @param client the maestro client
     */
    public void setMaestroClient(final MaestroClient client) {
        this.client = client;
    }

    /**
     * Get maestro client
     * @return MaestroClient
     */
    public MaestroClient getClient() {
        return client;
    }


    /**
     * Sets the worker options
     * @param workerOptions the worker options
     */
    public void setWorkerOptions(final WorkerOptions workerOptions) {
        this.workerOptions = workerOptions;
    }


    /**
     * Gets the worker options
     * @return the worker options
     */
    public WorkerOptions getWorkerOptions() {
        return workerOptions;
    }


    /**
     * Gets the current test (if any)
     * @return the current test
     */
    public Test getCurrentTest() {
        return currentTest;
    }


    /**
     * Sets the current test
     * @param currentTest the current test
     */
    public void setCurrentTest(Test currentTest) {
        this.currentTest = currentTest;
    }

    /**
     * Adds the shell prefix for command execution
     * @param command
     * @return
     */
    protected static String[] addShellPrefix(final String command) {
        return new String[] {"sh", "-c", command};
    }


    /**
     * Execute a command using the shell
     * @param command the command to execute
     * @return the process exit code
     * @throws IOException
     */
    protected static int executeOnShell(final String command) throws IOException {
        return executeOnShell(command, new File(System.getProperty("user.dir")));
    }


    /**
     * Execute a command using the shell
     * @param command the command to execute
     * @param workingDir the working directory
     * @return the process exit code
     * @throws IOException
     */
    protected static int executeOnShell(final String command, final File workingDir) throws IOException {
        logger.debug("Executing {}", command);

        Process process = new ProcessBuilder(addShellPrefix(command))
                .directory(workingDir)
                .redirectErrorStream(true)
                .start();


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("Subprocess output: {}", line);
            }

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                logger.info("Interrupted while waiting for the command to finish");
            }
        }

        return process.exitValue();
    }

    /**
     * Returns whether the test URL has parameters
     * @return true if it has parameters or false otherwise
     * @throws URISyntaxException
     */
    protected boolean hasParams() throws URISyntaxException {
        URLQuery urlQuery = new URLQuery(workerOptions.getBrokerURL());

        return urlQuery.count() > 0;
    }


    /**
     * Gets the test log directory (the location where the reports will be saved)
     * @return the test log directory
     */
    protected static File getTestLogDir() {
        File baseLogDir = getBaseLogDir();

        if (baseLogDir == null) {
            return null;
        }

        return TestLogUtils.nextTestLogDir(baseLogDir);
    }

    /**
     * Gets the base log directory
     * @return the base log directory
     */
    private static File getBaseLogDir() {
        final String baseLogDirStr = System.getProperty("maestro.log.dir");

        if (baseLogDirStr == null) {
            throw new MaestroException("The log directory is not set on the agent");
        }

        return new File(baseLogDirStr);
    }

    /**
     * Creates the sym links for a failed test
     */
    protected static void createTestFailSymlinks() {
        TestLogUtils.createSymlinks(getBaseLogDir(), true);
    }


    /**
     * Creates the sym links for a successful test
     */
    protected static void createTestSuccessSymlinks() {
        TestLogUtils.createSymlinks(getBaseLogDir(), false);
    }

}
