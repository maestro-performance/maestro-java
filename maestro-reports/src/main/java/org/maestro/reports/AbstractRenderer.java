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

package org.maestro.reports;

import com.google.common.base.Charsets;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.loader.CascadingResourceLocator;
import com.hubspot.jinjava.loader.FileLocator;
import org.apache.commons.io.IOUtils;
import org.maestro.common.Constants;
import org.maestro.reports.custom.FileExists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;

/**
 * A base class for rendering reports using Jinja2
 */
public abstract class AbstractRenderer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRenderer.class);
    protected static final String FAVICON_RESOURCE = "/org/maestro/reports/favicon.png";

    private final Jinjava jinjava;

    public AbstractRenderer() {
        JinjavaConfig config = new JinjavaConfig();

        jinjava = new Jinjava(config);
        jinjava.getGlobalContext().registerFilter(new FileExists());

        setupResourceLocator();
    }

    protected Jinjava getJinjava() {
        return jinjava;
    }

    protected void setupResourceLocator() {
        try {
            File currentDir = new File(Paths.get(".").toAbsolutePath().normalize().toString());
            FileLocator currentDirLocator = new FileLocator(currentDir);

            File templateDir = new File(Constants.HOME_DIR + File.separator + "templates");

            CascadingResourceLocator cr;
            if (templateDir.exists()) {
                FileLocator installDirLocator = new FileLocator(templateDir);

                cr = new CascadingResourceLocator(currentDirLocator, installDirLocator);
            }
            else {
                logger.warn("A template directory was not found, therefore ignoring");
                cr = new CascadingResourceLocator(currentDirLocator);
            }
            jinjava.setResourceLocator(cr);
        } catch (FileNotFoundException e) {
            logger.warn("Unable to find the current working directory: {}", e.getMessage(), e);
        }
    }

    /**
     * Render a report
     * @param name The resource name to be parsed
     * @param context the Jinja context containing the variables to be used
     * @return A String contained the parsed template
     * @throws Exception If unable to parse the template
     */
    protected String render(final String name, final Map<String, Object> context) throws Exception {
        String text;

        try (InputStream stream = this.getClass().getResourceAsStream(name)) {
            text = IOUtils.toString(stream, Charsets.UTF_8);
        }

        return jinjava.render(text, context);
    }

    /**
     * Copy static resources
     * @param path the path to copy to
     * @param resource the resource name
     * @param destinationName the destination name
     * @throws IOException on multiple I/O related errors
     */
    protected void copyResources(final File path, final String resource, final String destinationName) throws IOException {
        // Skip duplicate static resources
        File outputFile = new File(path, destinationName);
        if (outputFile.exists()) {
            return;
        }

        try (InputStream inputStream = new BufferedInputStream(this.getClass().getResourceAsStream(resource));
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile)))
        {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    /**
     * Render a report
     * @param context the Jinja context containing the variables to be used
     * @return A String contained the parsed template
     * @throws Exception If unable to parse the template
     */
    abstract public String render(final Map<String, Object> context) throws Exception;
}
