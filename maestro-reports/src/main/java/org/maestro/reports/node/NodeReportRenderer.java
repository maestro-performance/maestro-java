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

package org.maestro.reports.node;

import org.maestro.reports.AbstractRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;


public class NodeReportRenderer extends AbstractRenderer {
    private static final Logger logger = LoggerFactory.getLogger(NodeReportRenderer.class);

    public NodeReportRenderer() {
        super();
    }


    @Override
    public String render(final Map<String, Object> context) throws Exception {
        return super.render("/org/maestro/reports/modern/index-node.html", context);
    }

    public void copyResources(File path) throws IOException {
        super.copyResources(path, "/org/maestro/reports/favicon.png", "favicon.png");

    }
}
