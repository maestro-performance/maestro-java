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

package org.maestro.reports.dao;

import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.SutNodeInfo;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.List;

public class SutNodeInfoDao extends AbstractDao {
    public SutNodeInfoDao() {}

    public SutNodeInfoDao(TemplateBuilder tp) {
        super(tp);
    }

    public int insert(final SutNodeInfo sutNodeInfo) {
        return runInsert(
                "insert into sut_node_info(sut_node_name, sut_node_os_name, sut_node_os_arch, " +
                        "sut_node_os_version, sut_node_os_other, sut_node_hw_name, sut_node_hw_model, " +
                        "sut_node_hw_cpu, sut_node_hw_cpu_count, sut_node_hw_ram, " +
                        "sut_node_hw_disk_type, sut_node_hw_other) " +
                        "values(:sutNodeName, :sutNodeOsName, :sutNodeOsArch, :sutNodeOsVersion, " +
                        ":sutNodeOsOther, :sutNodeHwName, :sutNodeHwModel, :sutNodeHwCpu, " +
                        ":sutNodeHwCpuCount ,:sutNodeHwRam, :sutNodeHwDiskType, :sutNodeHwOther)",
                sutNodeInfo);
    }

    public List<SutNodeInfo> fetch() throws DataNotFoundException {
        return runQueryMany("select * from sut_node_info",
                new BeanPropertyRowMapper<>(SutNodeInfo.class));
    }

    public SutNodeInfo fetch(int sutNodeId) throws DataNotFoundException {
        return runQuery("select * from sut_node_info where sut_node_id = ?",
                new BeanPropertyRowMapper<>(SutNodeInfo.class),
                sutNodeId);
    }

    public List<SutNodeInfo> fetchByTest(int testId) throws DataNotFoundException {
        return runQueryMany("SELECT * FROM sut_node_info sn,test_sut_node_link tsnl " +
                        "WHERE sn.sut_node_id = tsnl.sut_node_id AND tsnl.test_id = ?",
                new BeanPropertyRowMapper<>(SutNodeInfo.class),
                testId);
    }
}
