package org.maestro.inspector.activemq;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.json.simple.JSONObject;
import org.maestro.common.inspector.types.JVMHeapInfo;

import javax.management.MalformedObjectNameException;

public class ArtemisDataReader {
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
    public JVMHeapInfo jvmHeapMemory() throws MalformedObjectNameException, J4pException {
        J4pReadRequest req = new J4pReadRequest("java.lang:type=Memory", "HeapMemoryUsage");

        // Throws if unable to read
        J4pReadResponse response = j4pClient.execute(req);

        JSONObject jo = response.getValue();

        long init = getLong(jo.get("init"));
        long committed = getLong(jo.get("committed"));
        long max = getLong(jo.get("max"));
        long used = getLong(jo.get("used"));

        return new JVMHeapInfo(init, committed, max, used);
    }
}
