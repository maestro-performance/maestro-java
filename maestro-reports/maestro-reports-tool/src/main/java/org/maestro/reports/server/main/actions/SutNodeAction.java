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

import org.apache.commons.cli.*;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.dao.TestSutNodeLinkDao;
import org.maestro.reports.dao.SutNodeInfoDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.TestSutNodeLinkRecord;
import org.maestro.reports.dto.SutNodeInfo;

import java.util.List;

public class SutNodeAction extends Action {
    private CommandLine cmdLine;
    private Options options;

    public SutNodeAction(String[] args) {
        processCommand(args);
    }

    @Override
    protected void processCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();

        options = new Options();

        options.addOption("h", "help", false, "prints the help");
        options.addOption("a", "action", true, "action (one of: insert, view, link)");
        options.addOption("s", "sut-node-id", true, "sut node id");
        options.addOption("t", "test-id", true, "test id");
        options.addOption(null, "node-name", true, "sut node name");
        options.addOption(null, "os-name", true, "sut node os name");
        options.addOption(null, "os-arch", true, "sut node os arch");
        options.addOption(null, "os-version", true, "sut node os version");
        options.addOption(null, "os-other", true, "other os version");
        options.addOption(null, "hw-name", true, "sut hardware name");
        options.addOption(null, "hw-model", true, "sut hardware model");
        options.addOption(null, "hw-cpu", true, "sut hardware cpu");
        options.addOption(null, "hw-cpu-count", true, "sut hardware cpu count");
        options.addOption(null, "hw-ram", true, "sut hardware ram");
        options.addOption(null, "hw-disk-type", true, "sut hardware disk type [hd, ssd]");
        options.addOption(null, "hw-other", true, "other hardware information");
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            help(options, -1);
        }

        if (cmdLine.hasOption("help")) {
            help(options, 0);
        }
    }

    private int add() {
        SutNodeInfoDao dao = new SutNodeInfoDao();
        SutNodeInfo dto = new SutNodeInfo();

        dto.setSutNodeName(cmdLine.getOptionValue("node-name"));
        dto.setSutNodeOsName(cmdLine.getOptionValue("os-name"));
        dto.setSutNodeOsArch(cmdLine.getOptionValue("os-arch"));
        dto.setSutNodeOsVersion(cmdLine.getOptionValue("os-version"));
        dto.setSutNodeOsOther(cmdLine.getOptionValue("os-other"));
        dto.setSutNodeHwName(cmdLine.getOptionValue("hw-name"));
        dto.setSutNodeHwModel(cmdLine.getOptionValue("hw-model"));
        dto.setSutNodeHwCpu(cmdLine.getOptionValue("hw-cpu"));
        dto.setSutNodeHwCpuCount(Integer.parseInt(cmdLine.getOptionValue("hw-cpu-count")));
        dto.setSutNodeHwRam(Integer.parseInt(cmdLine.getOptionValue("hw-ram")));
        dto.setSutNodeHwDiskType(cmdLine.getOptionValue("hw-disk-type"));
        dto.setSutNodeHwOther(cmdLine.getOptionValue("hw-other"));

        return dao.insert(dto);
    }

    private int view() {
        try {
            SutNodeInfoDao dao = new SutNodeInfoDao();
            List<SutNodeInfo> resources = dao.fetch();

            resources.stream().forEach(item -> System.out.println("SUT node: " + item));
        }
        catch (DataNotFoundException e) {
            System.out.println("There are not recorded SUT nodes in the database");
        }
        return 0;
    }

    private int link() {
        int testId;
        final String testIdStr = cmdLine.getOptionValue("test-id");
        if (testIdStr == null) {
            throw new MaestroException("Test ID is required");
        }

        try {
            testId = Integer.parseInt(testIdStr);
        } catch (Exception e) {
            throw new MaestroException("Invalid test test ID value %s", testIdStr);
        }


        int sutNodeId;
        final String sutNodeIdStr = cmdLine.getOptionValue("sut-node-id");
        if (sutNodeIdStr == null) {
            throw new MaestroException("Test ID is required");
        }

        try {
            sutNodeId = Integer.parseInt(sutNodeIdStr);
        } catch (Exception e) {
            throw new MaestroException("Invalid test test ID value %s", sutNodeIdStr);
        }

        TestSutNodeLinkRecord record = new TestSutNodeLinkRecord();

        record.setTestId(testId);
        record.setSutNodeId(sutNodeId);

        TestSutNodeLinkDao dao = new TestSutNodeLinkDao();
        dao.insert(record);

        return 0;
    }

    @Override
    public int run() {
        if (!cmdLine.hasOption("action")) {
            System.err.println("An action is required");

            return 1;
        }

        final String action = cmdLine.getOptionValue("action");

        switch (action) {
            case "insert": {
                System.out.println("New SUT node info created with ID: " + add());
                break;
            }
            case "view": {
                return view();
            }
            case "link": {
                return link();
            }
            default: {
                System.err.println("Invalid action: " + action);
                break;
            }
        }

        return 2;
    }
}
