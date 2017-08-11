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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRenderer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRenderer.class);

    private Map<String, Object> context = new HashMap<>();;

    public AbstractRenderer(Map<String, Object> context) {
        this.context = context;
    }

    protected String render(final String name) throws Exception {
        Jinjava jinjava = new Jinjava();

        String text;

        text = IOUtils.toString(this.getClass().getResourceAsStream(name), Charsets.UTF_8);

        return jinjava.render(text, context);
    }

    abstract public String render() throws Exception;
}
