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

package org.maestro.reports.server.main.actions;

import org.maestro.reports.common.organizer.Organizer;
import org.maestro.reports.common.organizer.PathBuilder;
import org.maestro.reports.dto.Report;

public class ReportOrganizer implements Organizer<Report> {
    private final String baseDir;

    public ReportOrganizer(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public String organize(final Report report) {
        return PathBuilder.build(baseDir, report.getTestId(), report.getTestNumber(), report.getTestHost());
    }
}
