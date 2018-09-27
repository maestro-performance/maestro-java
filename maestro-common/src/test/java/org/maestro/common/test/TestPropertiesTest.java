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
 *
 */

package org.maestro.common.test;

import org.junit.Test;
import org.maestro.common.test.properties.PropertyWriter;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class TestPropertiesTest {
    @Test
    public void testWriteTestProperties() throws Exception {
        runTest(fixtureTestProperties(), "test.properties");
    }

    @Test
    public void testWriteInspectorProperties() throws Exception {
        runTest(fixtureInspectorProperties(), "inspector.properties");
    }

    @Test
    public void testWriteSystemProperties() throws Exception {
        runTest(fixtureSystemProperties(), "system.properties");
    }

    private <T> void runTest(final T properties, final String fileName) throws Exception {
        String path = this.getClass().getResource(".").getPath();
        PropertyWriter writer = new PropertyWriter();

        File outFile = new File(path, fileName);

        writer.write(properties, outFile);

        assertTrue(outFile.exists());
    }

    private TestProperties fixtureTestProperties() {
        TestProperties tp = new TestProperties();

        tp.setApiName("testApi");
        tp.setApiVersion("0.9");
        tp.setBrokerUri("amqp://some/queue");
        tp.setDuration(100);
        tp.setFcl(10);
        tp.setLimitDestinations(30);
        tp.setProtocol("stomp");
        tp.setMessageSize(256);
        tp.setParallelCount(3);
        tp.setVariableSize(true);

        return tp;
    }

    private InspectorProperties fixtureInspectorProperties() {
        InspectorProperties ip = new InspectorProperties();

        ip.setJvmName("openjdk");
        ip.setJvmPackageVersion("2.0");
        ip.setJvmVersion("2.1");
        ip.setOperatingSystemArch("ppc64");
        ip.setOperatingSystemName("Linux");
        ip.setOperatingSystemVersion("4.10");
        ip.setProductName("openjdk");
        ip.setProductVersion("1");
        ip.setSystemCpuCount(4);
        ip.setSystemMemory(1024);
        ip.setSystemSwap(0);

        return ip;
    }

    private SystemProperties fixtureSystemProperties() {
        SystemProperties sp = new SystemProperties();

        sp.setWorkerJvmName("openjdk");
        sp.setWorkerJvmVersion("2.1");
        sp.setWorkerOperatingSystemArch("ppc64");
        sp.setWorkerOperatingSystemName("Linux");
        sp.setWorkerOperatingSystemVersion("4.10");

        sp.setWorkerSystemCpuCount(4);
        sp.setWorkerJVMMaxMemory(1024);

        return sp;
    }

}
