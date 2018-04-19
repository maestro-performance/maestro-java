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

package org.maestro.inspector.activemq.converter;

import org.json.simple.JSONObject;
import org.maestro.common.inspector.types.JVMMemoryInfo;
import org.maestro.inspector.activemq.JolokiaConverter;

import java.util.List;

import static org.maestro.inspector.activemq.JolokiaUtils.getLong;

public class JVMMemoryConverter implements JolokiaConverter {
    private final List<JVMMemoryInfo> jvmMemoryInfos;
    private final String parentPropertyName;

    public JVMMemoryConverter(final List<JVMMemoryInfo> jvmMemoryInfos, final String parentPropertyName) {
        this.jvmMemoryInfos = jvmMemoryInfos;
        this.parentPropertyName = parentPropertyName;
    }



    @Override
    public void convert(final String propertyName, JSONObject jsonObject) {
        Object tmp = jsonObject.get(parentPropertyName);
        long init = 0;
        long committed = 0;
        long max = 0;
        long used = 0;

        if (tmp instanceof JSONObject) {
            JSONObject childObject = (JSONObject) tmp;

            init = getLong(childObject.get("init"));
            committed = getLong(childObject.get("committed"));
            max = getLong(childObject.get("max"));
            used = getLong(childObject.get("used"));
        }

        JVMMemoryInfo jvmMemoryInfo = new JVMMemoryInfo(propertyName, init, committed, max, used);
        jvmMemoryInfos.add(jvmMemoryInfo);
    }
}
