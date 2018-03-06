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

package org.maestro.reports.index;

import org.apache.commons.io.FileUtils;
import org.maestro.reports.AbstractRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;


public class IndexRenderer extends AbstractRenderer {
    private static final Logger logger = LoggerFactory.getLogger(IndexRenderer.class);

    public IndexRenderer() {
        super();
    }

    @Override
    public String render(final Map<String, Object> context) throws Exception {
        return super.render("/org/maestro/reports/modern/index-main.html", context);
    }

    public void copyResources(File path) throws IOException {
        super.copyResources(path, "/org/maestro/reports/favicon.png", "favicon.png");

        // Template resources
        URL resourcesUrl = this.getClass().getResource("/org/maestro/reports/modern/resources");

        File resources = new File(resourcesUrl.getPath());

        FileUtils.copyDirectory(resources, new File(path, "resources"));


    }
}
