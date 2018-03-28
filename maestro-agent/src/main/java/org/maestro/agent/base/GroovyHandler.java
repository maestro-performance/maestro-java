package org.maestro.agent.base;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.control.CompilationFailedException;
import org.maestro.client.MaestroReceiverClient;
import org.maestro.common.agent.AgentHandler;
import org.maestro.common.exceptions.MaestroException;
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
    private MaestroReceiverClient client;

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

    private void runCallback(final File file, String callbackName) {
        GroovyObject groovyObject = getObject(file);

        groovyObject.invokeMethod("setMaestroClient", this.client);
        groovyObject.invokeMethod(callbackName, context);
    }

    public void runCallbacks(){
        for (File file : fileList){
            runCallback(file, "handle");
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
}