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

import java.util.*;

public class ReportContextBuilder {
    private ReportContextBuilder() {}

    public static Map<String, Object> toContext(List<ReportFile> reportFiles) {
        Map<String, Object> context = new HashMap<>();

        Set<String> nodes = new HashSet<>();
        Set<String> nodeTypes = new HashSet<>();
        Set<Integer> tests = new HashSet<>();
        Set<ReportDirInfo> reportDirs = new HashSet<>();

        for (ReportFile reportFile : reportFiles) {
            nodes.add(reportFile.getNodeHost());
            nodeTypes.add(reportFile.getNodeType().getValue());
            tests.add(reportFile.getTestNum());
            reportDirs.add(new ReportDirInfo(reportFile.getReportDir(), reportFile.getNodeType().getValue()));
        }

        context.put("nodes", nodes);
        context.put("nodeTypes", nodeTypes);
        context.put("tests", tests);
        context.put("reportFiles", reportFiles);
        context.put("reportDirs", reportDirs);

        return context;
    }


}
