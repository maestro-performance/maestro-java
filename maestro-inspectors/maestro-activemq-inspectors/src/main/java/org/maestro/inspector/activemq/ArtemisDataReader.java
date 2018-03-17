package org.maestro.inspector.activemq;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.json.simple.JSONObject;
import org.maestro.common.inspector.types.JVMMemoryInfo;
import org.maestro.common.inspector.types.OSInfo;
import org.maestro.inspector.activemq.converter.MapConverter;
import org.maestro.inspector.activemq.converter.JVMMemoryConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.maestro.inspector.activemq.JolokiaUtils.*;

public class ArtemisDataReader {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisDataReader.class);
    private J4pClient j4pClient;

    public ArtemisDataReader(J4pClient j4pClient) {
        this.j4pClient = j4pClient;
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

    private void assignMemoryInfo(final JolokiaConverter converter, final Object key, final Object object) {
        if (logger.isTraceEnabled()) {
            logger.trace("Processing returned JSON Key {} with value: {}", key, object);
        }

        String jolokiaPropertyName = "";
        if (key instanceof String) {
            String tmp = (String) key;
            logger.debug("Checking property name/group {}", tmp);

            Pattern pattern = Pattern.compile(".*name=(.*),.*");
            Matcher matcher = pattern.matcher(tmp);

            if (matcher.matches()) {
                jolokiaPropertyName = matcher.group(1);

                logger.debug("Reading property name/group '{}'", jolokiaPropertyName);
            }
        }

        converter.convert(jolokiaPropertyName, object);
    }

    /**
     * Reads JVM Heap heap memory information information (might be JVM-specific)
     * @return JVM heap memory information
     * @throws MalformedObjectNameException
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    public List<JVMMemoryInfo> jvmEdenSpace() throws MalformedObjectNameException, J4pException {
        J4pReadRequest req = new J4pReadRequest("java.lang:name=*,type=MemoryPool", "Usage");

        // Throws if unable to read
        J4pReadResponse response = j4pClient.execute(req);

        JSONObject jo = response.getValue();



        if (logger.isDebugEnabled()) {
            logger.debug("JSON response: {}", jo.toString());
        }

        List<JVMMemoryInfo> jvmMemoryInfos = new LinkedList<>();
        JolokiaConverter jolokiaConverter = new JVMMemoryConverter(jvmMemoryInfos);
        jo.forEach((key, value) -> assignMemoryInfo(jolokiaConverter, key, value));

        return jvmMemoryInfos;
    }

    /**
     * Reads operating system information information (might be JVM-specific)
     * @return operating system information
     * @throws MalformedObjectNameException
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    public OSInfo operatingSystem() throws MalformedObjectNameException, J4pException {
        J4pReadRequest req = new J4pReadRequest("java.lang:type=OperatingSystem", "");

        // Throws if unable to read
        J4pReadResponse response = j4pClient.execute(req);

        JSONObject jo = response.getValue();

        if (logger.isDebugEnabled()) {
            logger.debug("JSON response: {}", jo.toString());
        }

        Map<String, Object> osProperties = new HashMap<>();
        JolokiaConverter jolokiaConverter = new MapConverter(osProperties);
        jo.forEach((key, value) -> assignMemoryInfo(jolokiaConverter, key, value));

        return new OSInfo(osProperties);
    }
}
