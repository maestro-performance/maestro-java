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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.control.CompilationFailedException;
import org.maestro.client.MaestroReceiverClient;
import org.maestro.common.agent.AgentHandler;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.contrib.groovy.GroovyCallbackWalker;
import org.maestro.contrib.groovy.GroovyClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by opiske on 5/10/16.
 */
public class GroovyHandler implements AgentHandler {
    private static final Logger logger = LoggerFactory.getLogger(GroovyHandler.class);

    private Map<String, Object> context;
    private List<File> fileList;
    private final MaestroReceiverClient client;

    private MaestroNote maestroNote;
    private WorkerOptions workerOptions;

    public GroovyHandler(MaestroReceiverClient client) {
        this.client = client;
    }

    private GroovyObject getObject(final File file) {
        GroovyClasspathHelper classpathHelper = GroovyClasspathHelper.getInstance();
        GroovyClassLoader loader = classpathHelper.getLoader();

        // Parses the class
        Class<?> groovyClass;
        try {
            groovyClass = loader.parseClass(file);
        } catch (CompilationFailedException e) {
            throw new MaestroException("The script has errors: " + e.getMessage(),
                    e);
        } catch (IOException e) {
            throw new MaestroException("Input/output error: " + e.getMessage(),
                    e);
        }

        // Instantiate the object
        GroovyObject groovyObject;
        try {
            groovyObject = (GroovyObject) groovyClass.newInstance();
        } catch (InstantiationException e) {
            throw new MaestroException("Unable to instantiate object: "
                    + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new MaestroException("Illegal access: " + e.getMessage(),
                    e);
        }

        return groovyObject;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    private void runCallback(final File file) {
        GroovyObject groovyObject = getObject(file);

        groovyObject.invokeMethod("setMaestroNote", this.maestroNote);
        groovyObject.invokeMethod("setWorkerOptions", this.workerOptions);
        groovyObject.invokeMethod("setMaestroClient", this.client);
        groovyObject.invokeMethod("handle", context);
    }

    public void runCallbacks(){
        for (File file : fileList){
            runCallback(file);
        }
    }

    @Override
    public void setInitialPath(File initialPath) throws Exception {
        GroovyCallbackWalker walker = new GroovyCallbackWalker();
        logger.debug("Processing {}", initialPath.getAbsolutePath());

        fileList = walker.load(initialPath);
    }

    public MaestroReceiverClient getClient() {
        return client;
    }

    public MaestroNote getMaestroNote() {
        return maestroNote;
    }

    public void setMaestroNote(MaestroNote maestroNote) {
        this.maestroNote = maestroNote;
    }

    public WorkerOptions getWorkerOptions() {
        return workerOptions;
    }

    public void setWorkerOptions(WorkerOptions workerOptions) {
        this.workerOptions = workerOptions;
    }
}