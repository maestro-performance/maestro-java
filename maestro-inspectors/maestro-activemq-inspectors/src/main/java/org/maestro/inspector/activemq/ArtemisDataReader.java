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

package org.maestro.inspector.activemq;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.json.simple.JSONObject;
import org.maestro.common.inspector.types.*;
import org.maestro.inspector.activemq.converter.JVMMemoryConverter;
import org.maestro.inspector.activemq.converter.MapConverter;
import org.maestro.inspector.activemq.converter.QueueInfoConverter;
import org.maestro.inspector.activemq.types.ArtemisProductInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.maestro.inspector.activemq.JolokiaUtils.getLong;

public class ArtemisDataReader {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisDataReader.class);
    private final DefaultJolokiaParser defaultJolokiaParser = new DefaultJolokiaParser();

    private final J4pClient j4pClient;

    public ArtemisDataReader(J4pClient j4pClient) {
        this.j4pClient = j4pClient;
    }

    private JSONObject getJsonObject(String s, String s2) throws MalformedObjectNameException, J4pException {
        J4pReadRequest req = new J4pReadRequest(s, s2);

        // Throws if unable to read
        J4pReadResponse response = j4pClient.execute(req);
        assert response != null : "The response object must never be null";

        JSONObject jo = response.getValue();

        if (logger.isTraceEnabled()) {
            logger.trace("json response: {}", jo.toString());
        }
        return jo;
    }

    /**
     * Reads JVM Heap heap memory information information (might be JVM-specific)
     * @return JVM heap memory information
     * @throws MalformedObjectNameException JMX internal/specific error
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    public JVMMemoryInfo jvmHeapMemory() throws MalformedObjectNameException, J4pException {
        JSONObject jo = getJsonObject("java.lang:type=Memory", "HeapMemoryUsage");

        long init = getLong(jo.get("init"));
        long committed = getLong(jo.get("committed"));
        long max = getLong(jo.get("max"));
        long used = getLong(jo.get("used"));

        return new JVMMemoryInfo("Heap", init, committed, max, used);
    }


    /**
     * Reads JVM Heap heap memory information information (might be JVM-specific)
     * @return JVM heap memory information
     * @throws MalformedObjectNameException JMX internal/specific error
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    @SuppressWarnings("unchecked")
    public List<JVMMemoryInfo> jvmMemoryAreas() throws MalformedObjectNameException, J4pException {
        JSONObject jo = getJsonObject("java.lang:name=*,type=MemoryPool", "Usage");

        List<JVMMemoryInfo> jvmMemoryInfos = new LinkedList<>();
        JolokiaConverter jolokiaConverter = new JVMMemoryConverter(jvmMemoryInfos, "Usage");
        jo.forEach((key, value) -> defaultJolokiaParser.parse(jolokiaConverter, key, value));

        return jvmMemoryInfos;
    }

    /**
     * Reads operating system information information (might be JVM-specific)
     * @return operating system information
     * @throws MalformedObjectNameException JMX internal/specific error
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    @SuppressWarnings("unchecked")
    public OSInfo operatingSystem() throws MalformedObjectNameException, J4pException {
        JSONObject jo = getJsonObject("java.lang:type=OperatingSystem", "");

        Map<String, Object> osProperties = new HashMap<>();
        JolokiaConverter jolokiaConverter = new MapConverter(osProperties);
        jo.forEach((key, value) -> defaultJolokiaParser.parse(jolokiaConverter, key, value));

        return new OSInfo(osProperties);
    }


    /**
     * Reads runtime information information (might be JVM-specific)
     * @return operating system information
     * @throws MalformedObjectNameException JMX internal/specific error
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    @SuppressWarnings("unchecked")
    public RuntimeInfo runtimeInformation() throws MalformedObjectNameException, J4pException {
        JSONObject jo = getJsonObject("java.lang:type=Runtime", "");

        Map<String, Object> osProperties = new HashMap<>();
        JolokiaConverter jolokiaConverter = new MapConverter(osProperties);
        jo.forEach((key, value) -> defaultJolokiaParser.parse(jolokiaConverter, key, value));

        return new RuntimeInfo(osProperties);
    }


    /**
     * Read queue information
     * @return queue information
     * @throws MalformedObjectNameException JMX internal/specific error
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    @SuppressWarnings("unchecked")
    public QueueInfo queueInformation() throws MalformedObjectNameException, J4pException {
        JSONObject jo = getJsonObject("org.apache.activemq.artemis:address=*,broker=*,component=addresses,queue=*,*", "");

        Map<String, Object> queueProperties = new HashMap<>();
        QueueInfoConverter jolokiaConverter = new QueueInfoConverter(queueProperties);
        jo.forEach((key, value) -> defaultJolokiaParser.parseQueueInfo(jolokiaConverter, key, value));

        return new QueueInfo(queueProperties);
    }


    /**
     * Read product information
     * @return the product information
     * @throws MalformedObjectNameException JMX internal/specific error
     * @throws J4pException if unable to read (ie: forbidden to read the value)
     */
    @SuppressWarnings("unchecked")
    public ProductInfo productInformation() throws MalformedObjectNameException, J4pException {
        JSONObject jo = getJsonObject("org.apache.activemq.artemis:broker=*", "Version");

        Map<String, Object> productProperties = new HashMap<>();
        JolokiaConverter converter = new MapConverter(productProperties);
        jo.forEach((key, value) -> defaultJolokiaParser.parse(converter, key, value));

        return new ArtemisProductInfo(productProperties);
    }
}
