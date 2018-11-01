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

package org.maestro.reports.controllers;

import io.javalin.Context;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileListController extends AbstractReportFileController {
    class Archive {
        private String name;
        private String link;

        public Archive() {}

        public Archive(final File file, int testId) {
            this.name = file.getName();
            this.link = "/raw/report/" + testId + "/files/" + name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
    private final ReportDao reportDao = new ReportDao();

    @Override
    public void handle(Context context) {
        try {
            int id = Integer.parseInt(context.param("id"));

            Report report = reportDao.fetch(id);

            File location = new File(report.getLocation());

            if (!location.exists()) {
                context.status(404);
                context.result(String.format("Not found: %s", location.getPath()));

                return;
            }

            if (!location.isDirectory()) {
                throw new MaestroException("Invalid file type: expected a directory, but got something else");
            }

            File files[] = location.listFiles(file -> {
                String extensions[] = { ".csv", ".hdr", ".dat", ".properties", ".json", ".xz"};

                for (String extension : extensions) {
                    if (file.getName().endsWith(extension)) {
                        return true;
                    }
                }

                return false;
            });

            List<Archive> fileList = Arrays.asList(files).stream().map(f -> new Archive(f, id)).collect(Collectors.toList());

            context.json(fileList);
        }
        catch (DataNotFoundException e) {
            context.status(404);
            context.result(String.format("Not found: %s", e.getMessage()));
        }
        catch (Throwable t) {
            context.status(500);
            context.result(String.format("Internal server error: %s", t.getMessage()));
        }
    }
}
