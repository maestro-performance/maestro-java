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

package org.maestro.reports.composed;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComposedIndexGenerator {
    public static void generate(final File baseDir) throws Exception {
        ComposedIndexWalker walker = new ComposedIndexWalker();

        List<ComposedProperties> propertiesList = walker.generate(baseDir);

        Map<String, Object> context = new HashMap<>();
        context.put("propertiesList", propertiesList);

        ComposedIndexRenderer renderer = new ComposedIndexRenderer();

        File outFile = new File(baseDir, "index.html");

        FileUtils.writeStringToFile(outFile, renderer.render(context), StandardCharsets.UTF_8);
        renderer.copyResources(baseDir);
    }
}
