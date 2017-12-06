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

package net.orpiske.mpt.reports.node;

import net.orpiske.mpt.reports.ReportDirInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class NodeContextBuilder {
    private static Logger logger = LoggerFactory.getLogger(NodeContextBuilder.class);

    private NodeContextBuilder() {}

    public static Map<String, Object> toContext(ReportDirInfo reportDirInfo, File baseDir) {
        Map<String, Object> context = new HashMap<>();
        File file = new File(baseDir, reportDirInfo.getReportDir());

        context.put("node", file.getName());
        context.put("nodeType", reportDirInfo.getNodeType());
        context.put("testNumber", file.getParentFile().getName());
        context.put("result", file.getParentFile().getParentFile().getName());
        context.put("reportDirInfo", reportDirInfo);
        context.put("baseDir", baseDir);

        loadProperties(context, new File(file,"test.properties"));
        loadProperties(context, new File(file,"broker.properties"));
        loadProperties(context, new File(file,"rate.properties"));

        return context;
    }

    private static void loadProperties(Map<String, Object> context, File testProperties) {
        if (testProperties.exists()) {
            Properties prop = new Properties();

            try (FileInputStream in = new FileInputStream(testProperties)) {
                prop.load(in);

                for (Map.Entry e : prop.entrySet()) {
                    logger.debug("Adding entry {} with value {}", e.getKey(), e.getValue());
                    context.put((String) e.getKey(), e.getValue());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            logger.debug("There are no properties file at {}", testProperties.getPath());
        }
    }


}
