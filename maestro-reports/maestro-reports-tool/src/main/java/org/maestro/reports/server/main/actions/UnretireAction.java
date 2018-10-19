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

import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;

import java.util.List;

public class UnretireAction extends ManageAction {
    private final ReportDao reportDao = new ReportDao();

    public UnretireAction(String[] args) {
        super(args);
    }

    public int run() {
        try {
            List<Report> reports = reportDao.fetchByTestId(testId);

            for (Report report : reports) {
                if (comments != null) {
                    report.setTestComments(comments);
                }

                report.setRetired(false);
                report.setRetiredDate(null);

                reportDao.update(report);
            }

            return 0;
        } catch (DataNotFoundException e) {
            System.err.println("Unable to unretire the records: no records matching the given ID");

            return 1;
        }

        catch (Exception e) {
            System.err.println("Unable to unretire the reports: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
