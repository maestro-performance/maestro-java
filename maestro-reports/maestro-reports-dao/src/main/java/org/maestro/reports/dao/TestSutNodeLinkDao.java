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
import org.maestro.reports.dto.TestSutNodeLinkRecord;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.List;

public class TestSutNodeLinkDao extends AbstractDao {

    public TestSutNodeLinkDao() {}

    public TestSutNodeLinkDao(TemplateBuilder tp) {
        super(tp);
    }

    public void insert(final TestSutNodeLinkRecord record) {
        runEmptyInsert("insert into test_sut_node_link(test_id, sut_node_id) values(:testId, :sutNodeId)",
                record);
    }

    public List<TestSutNodeLinkRecord> fetch() throws DataNotFoundException {
        return runQueryMany("select * from test_sut_node_link",
                new BeanPropertyRowMapper<>(TestSutNodeLinkRecord.class));
    }

    public TestSutNodeLinkRecord fetch(int testId) throws DataNotFoundException {
        return runQuery("select * from test_sut_node_link where test_id = ?",
                new BeanPropertyRowMapper<>(TestSutNodeLinkRecord.class),
                testId);
    }
}
