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

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationMap;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class NodeContextBuilder {
    private static Logger logger = LoggerFactory.getLogger(NodeContextBuilder.class);

    private NodeContextBuilder() {}

    public static Map<String, Object> toContext(ReportDirInfo reportDirInfo) {
        Map<String, Object> context = new HashMap<>();
        File file = new File(reportDirInfo.getReportDir());

        context.put("node", file.getName());
        context.put("nodeType", reportDirInfo.getNodeType());
        context.put("testNumber", file.getParentFile().getName());
        context.put("result", file.getParentFile().getParentFile().getName());

        loadProperties(context, new File(file,"test.properties"));
        loadProperties(context, new File(file,"broker.properties"));

        return context;
    }

    private static void loadProperties(Map<String, Object> context, File testProperties) {
        if (testProperties.exists()) {
            Properties prop = new Properties();

            try (FileInputStream in = new FileInputStream(testProperties)) {
                prop.load(in);

                for (Map.Entry e : prop.entrySet()) {
                    logger.debug("Addding entry {} with value {}", e.getKey(), e.getValue());
                    context.put((String) e.getKey(), e.getValue());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
