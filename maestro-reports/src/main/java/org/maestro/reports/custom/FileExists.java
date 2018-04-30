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

package org.maestro.reports.custom;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileExists implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(FileExists.class);

    @Override
    public Object filter(Object var, JinjavaInterpreter jinjavaInterpreter, String... strings) {
        String filename = (String) var;
        logger.trace("Processing filter argument for {} with arg len {}", filename,
                strings.length);

        if (strings.length < 1) {
            throw new RuntimeException("Not enough arguments (got " + strings.length + " expected 1)");
        }

        File file = new File(strings[0] + File.separator + filename);

        logger.trace("Checking whether the path {} exists", file.getPath());
        return file.exists();
    }

    @Override
    public String getName() {
        return "fe";
    }
}
