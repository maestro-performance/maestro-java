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
import org.apache.commons.io.IOUtils;
import org.maestro.reports.AbstractRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ComposedIndexRenderer extends AbstractRenderer {
    private static final Logger logger = LoggerFactory.getLogger(ComposedIndexRenderer.class);
    private final List<String> filteredResources = Collections.singletonList("resources");
    @Override
    public String render(Map<String, Object> context) throws Exception {
        return super.render("/org/maestro/reports/modern/index-composed.html", context);
    }

//    public void copyResources(File path) throws IOException {
//        super.copyResources(path, "/org/maestro/reports/favicon.png", "favicon.png");
//    }

    protected void zipCopying(ZipEntry entry, JarFile jarFile, File path, String name) throws IOException {

        // Skip those files which filtered subfolder
        for (String filteredResource : filteredResources) {
            if (!name.contains(filteredResource)) {
                return;
            }
        }

        logger.debug("Building destination path with {} and {}", path.getPath(), name);
        String destPath = path.getPath() + File.separator + name;

        if(entry.isDirectory()){
            File file = new File(destPath);
            file.mkdirs();
        }
        else{
            logger.debug("Copying file {} to {}", entry.getName(), destPath);
            try(InputStream inputStream = jarFile.getInputStream(entry);
                FileOutputStream outputStream = new FileOutputStream(destPath)
            ){
                IOUtils.copy(inputStream, outputStream);
            }
        }
    }

    protected void zipCopying(ZipEntry entry, JarFile jarFile, File path) throws IOException {
        String name = entry.getName().replace("org/maestro/reports/modern/", "");

        zipCopying(entry, jarFile, path, name);
    }


    public void copyResources(File path) throws IOException {
        super.copyResources(path, FAVICON_RESOURCE, "favicon.png");

        File jarPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        try {
            JarFile jarFile = new JarFile(jarPath);

            Enumeration<JarEntry> entries = jarFile.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();

                zipCopying(entry, jarFile, path);
            }
        }
        catch (FileNotFoundException e) {
            String tmp = this.getClass().getResource("/org/maestro/reports/modern/resources").getPath();
            File sourceDir = new File(tmp);

            logger.debug("Copying directory {} to {}", sourceDir, path);
            FileUtils.copyDirectory(sourceDir, new File(path, "resources"));
        }
    }
}
