package org.maestro.inspector.activemq;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.json.simple.JSONObject;
import org.maestro.common.inspector.types.JVMMemoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtemisDataReader {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisDataReader.class);
    private J4pClient j4pClient;

    public ArtemisDataReader(J4pClient j4pClient) {
        this.j4pClient = j4pClient;
    }

    private long getLong(Object object, long defaultValue) {
        if (object == null) {
            return defaultValue;
        }

        if (object instanceof Long) {
            return (Long) object;
        }

        return defaultValue;
    }

    private long getLong(Object object) {
        return getLong(object, 0);
    }

    /**
     * Reads JVM Heap heap memory information information (might be JVM-specific)
     * @return JVM heap memory information
     * @throws MalformedObjectNameException
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    public JVMMemoryInfo jvmHeapMemory() throws MalformedObjectNameException, J4pException {
        J4pReadRequest req = new J4pReadRequest("java.lang:type=Memory", "HeapMemoryUsage");

        // Throws if unable to read
        J4pReadResponse response = j4pClient.execute(req);

        JSONObject jo = response.getValue();

        long init = getLong(jo.get("init"));
        long committed = getLong(jo.get("committed"));
        long max = getLong(jo.get("max"));
        long used = getLong(jo.get("used"));

        return new JVMMemoryInfo("Heap", init, committed, max, used);
    }

    private void assignMemoryInfo(final List<JVMMemoryInfo> infos, final Object key, final Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace("Processing returned JSON Key {} with value: {}", key, object);
        }

        String memoryAreaName = "";
        if (key instanceof String) {
            String tmp = (String) key;
            logger.debug("Checking key {}", tmp);

            Pattern pattern = Pattern.compile(".*name=.*,.*");
            Matcher matcher = pattern.matcher(tmp);

            if (matcher.matches()) {
                memoryAreaName = matcher.group();

                logger.debug("Reading information from memory area '{}'", memoryAreaName);
            }
        }

        if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;

            long init = getLong(jsonObject.get("init"));
            long committed = getLong(jsonObject.get("committed"));
            long max = getLong(jsonObject.get("max"));
            long used = getLong(jsonObject.get("used"));

            JVMMemoryInfo jvmMemoryInfo = new JVMMemoryInfo(memoryAreaName, init, committed, max, used);
            infos.add(jvmMemoryInfo);
        }
    }

    /**
     * Reads JVM Heap heap memory information information (might be JVM-specific)
     * @return JVM heap memory information
     * @throws MalformedObjectNameException
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    public List<JVMMemoryInfo> jvmEdenSpace() throws MalformedObjectNameException, J4pException {
        /**
         * { "java.lang:name=Metaspace,type=MemoryPool":{"Usage":{"init":0,"committed":37961728,"max":-1,"used":36971200}},
         *      "java.lang:name=Compressed Class Space,type=MemoryPool":{"Usage":{"init":0,"committed":4456448,"max":1073741824,"used":4151008}},
         *      "java.lang:name=G1 Eden Space,type=MemoryPool":{"Usage":{"init":28311552,"committed":337641472,"max":-1,"used":25165824}},
         *      "java.lang:name=G1 Old Gen,type=MemoryPool":{"Usage":{"init":508559360,"committed":198180864,"max":2147483648,"used":24741000}},
         *      "java.lang:name=G1 Survivor Space,type=MemoryPool":{"Usage":{"init":0,"committed":1048576,"max":-1,"used":1048576}},
         *      "java.lang:name=Code Cache,type=MemoryPool":{"Usage":{"init":2555904,"committed":15794176,"max":251658240,"used":15639168}}
         *}
        */
         J4pReadRequest req = new J4pReadRequest("java.lang:name=*,type=MemoryPool", "Usage");

        // Throws if unable to read
        J4pReadResponse response = j4pClient.execute(req);

        JSONObject jo = response.getValue();

        List<JVMMemoryInfo> infos = new LinkedList<>();

        if (logger.isDebugEnabled()) {
            logger.debug("JSON response: {}", jo.toString());
        }

        jo.forEach((key, value) -> assignMemoryInfo(infos, key, value));

        return infos;
    }
}
