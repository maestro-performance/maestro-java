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

import org.maestro.reports.AbstractRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


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

        File jarPath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        JarFile jarFile = new JarFile(jarPath);

        Enumeration entries = jarFile.entries();

        while(entries.hasMoreElements()){
            ZipEntry entry = (ZipEntry)entries.nextElement();

            String name = entry.getName().replace("org/maestro/reports/modern/", "");

            // Skip those files which aren't in resources subfolder
            if(!name.contains("resources"))
                continue;

            if(entry.isDirectory()){
                String destPath = path.getPath() + File.separator + name;
                File file = new File(destPath);
                file.mkdirs();
            }
            else{
                String destPath = path.getPath() + File.separator + name;

                // This cause the performance problem, it copy only files from resources but it's really slow (15-20s, unzip can do it in 1s)
                try(InputStream inputStream = jarFile.getInputStream(entry);
                    FileOutputStream outputStream = new FileOutputStream(destPath);
                ){
                    int data;
                    while((data = inputStream.read()) != -1){
                        outputStream.write(data);
                    }
                }
            }
        }
    }
}
