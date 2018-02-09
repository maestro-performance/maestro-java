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

package net.orpiske.mpt.reports;

import com.google.common.base.Charsets;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.loader.CascadingResourceLocator;
import com.hubspot.jinjava.loader.FileLocator;
import net.orpiske.mpt.common.Constants;
import net.orpiske.mpt.reports.custom.FileExists;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;

public abstract class AbstractRenderer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRenderer.class);

    private Map<String, Object> context;

    private Jinjava jinjava;

    public AbstractRenderer(Map<String, Object> context) {
        this.context = context;

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
            logger.warn("Unable to find the current working directory: " + e.getMessage(), e);
        }
    }

    protected String render(final String name) throws Exception {
        String text;

        text = IOUtils.toString(this.getClass().getResourceAsStream(name), Charsets.UTF_8);

        return jinjava.render(text, context);
    }

    protected void copyResources(File path, String resource, String outName) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        // Skip duplicate static resources
        File outputFile = new File(path, outName);
        if (outputFile.exists()) {
            return;
        }

        try {
            inputStream = this.getClass().getResourceAsStream(resource);
            outputStream = new FileOutputStream(outputFile);

            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    abstract public String render() throws Exception;
}
