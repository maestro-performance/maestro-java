package org.maestro.inspector.activemq.converter;

import org.json.simple.JSONObject;
import org.maestro.common.inspector.types.JVMMemoryInfo;
import org.maestro.inspector.activemq.JolokiaConverter;

import java.util.List;

import static org.maestro.inspector.activemq.JolokiaUtils.*;

public class JVMMemoryConverter implements JolokiaConverter {
    public List<JVMMemoryInfo> jvmMemoryInfos;

    public JVMMemoryConverter(final List<JVMMemoryInfo> jvmMemoryInfos) {
        this.jvmMemoryInfos = jvmMemoryInfos;
    }



    @Override
    public void convert(final String propertyName, JSONObject jsonObject) {
        long init = getLong(jsonObject.get("init"));
        long committed = getLong(jsonObject.get("committed"));
        long max = getLong(jsonObject.get("max"));
        long used = getLong(jsonObject.get("used"));

        JVMMemoryInfo jvmMemoryInfo = new JVMMemoryInfo(propertyName, init, committed, max, used);
        jvmMemoryInfos.add(jvmMemoryInfo);
    }
}
