package org.maestro.inspector.activemq;

import org.jolokia.client.BasicAuthenticator;
import org.jolokia.client.J4pClient;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.common.inspector.types.*;
import org.maestro.inspector.activemq.writers.JVMMemoryInfoWriter;
import org.maestro.inspector.activemq.writers.OSInfoWriter;
import org.maestro.inspector.activemq.writers.QueueInfoWriter;
import org.maestro.inspector.activemq.writers.RuntimeInfoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A inspector class for Apache ActiveMQ Artemis
 */
public class ArtemisInspector implements MaestroInspector {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisInspector.class);
    private long startedEpochMillis = Long.MIN_VALUE;
    private boolean running = false;
    private String url;
    private String user;
    private String password;
    private TestDuration duration;

    private ArtemisDataReader artemisDataReader;
    private J4pClient j4p;

    private JVMMemoryInfoWriter heapMemoryWriter = new JVMMemoryInfoWriter();
    private JVMMemoryInfoWriter jvmMemoryAreasWriter = new JVMMemoryInfoWriter();
    private RuntimeInfoWriter runtimeInfoWriter = new RuntimeInfoWriter();
    private OSInfoWriter osInfoWriter = new OSInfoWriter();
    private QueueInfoWriter queueInfoWriter = new QueueInfoWriter();

    public ArtemisInspector() {

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDuration(final String duration) throws DurationParseException {
        this.duration = TestDurationBuilder.build(duration);
    }

    public boolean isRunning() {
        return running;
    }

    private void connect() {
        j4p = J4pClient.url(url)
                .user(user)
                .password(password)
                .authenticator(new BasicAuthenticator().preemptive())
                .connectionTimeout(3000)
                .build();

        artemisDataReader = new ArtemisDataReader(j4p);
    }


    /*
     OSInfo{osProperties={
        OpenFileDescriptorCount=481,
        CommittedVirtualMemorySize=8410505216,
        FreePhysicalMemorySize=33030639616,
        SystemLoadAverage=5.52,
        Arch=amd64,
        ProcessCpuLoad=4.587047937367625E-5,
        FreeSwapSpaceSize=33780920320,
        TotalPhysicalMemorySize=67418030080,
        Name=Linux,
        ObjectName=null,
        TotalSwapSpaceSize=33780920320,
        ProcessCpuTime=449800000000,
        MaxFileDescriptorCount=1048576,
        SystemCpuLoad=0.03170793193256094,
        Version=4.15.6-300.fc27.x86_64, AvailableProcessors=8}
        }


    DEBUG 13:36:36,597  org.maestro.inspector.activemq.ArtemisInspector - Runtime information:
    RuntimeInfo{
        osProperties={
            SpecVendor=Oracle Corporation,
            ClassPath=/opt/maestro/sut/apache-artemis-2.4.0/lib/artemis-boot.jar,
            Uptime=2907673, VmName=OpenJDK 64-Bit Server VM, StartTime=1521460088938,
            SpecName=Java Virtual Machine Specification,
            VmVersion=25.161-b14, ManagementSpecVersion=1.2,
            Name=1@c361ba3a8420, ObjectName=null, VmVendor=Oracle Corporation,
            LibraryPath=/opt/maestro/sut/apache-artemis-2.4.0/bin/lib/linux-x86_64,
            BootClassPath=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre/lib/resources.jar:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre/lib/rt.jar:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre/lib/sunrsasign.jar:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre/lib/jsse.jar:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre/lib/jce.jar:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre/lib/charsets.jar:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre/lib/jfr.jar:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.161-0.b14.el7_4.x86_64/jre/classes:/opt/maestro/sut/apache-artemis-2.4.0/lib/jboss-logmanager-2.0.3.Final.jar,
            SpecVersion=1.8,
            SystemProperties=null,
            BootClassPathSupported=true}
            }
DEBUG 13:36:36,615  org.maestro.inspector.activemq.ArtemisInspector - Heap Memory Usage: JVMMemoryInfo{
    memoryAreaName='Heap', initial=536870912, committed=893386752, max=2147483648, used=463146784}

     */
    public int start() throws Exception {
        try {
            startedEpochMillis = System.currentTimeMillis();
            running = true;

            if (url == null) {
                logger.error("No management interface was given for the test. Therefore, ignoring");
                return 1;
            }

            connect();

            OSInfo osInfo = artemisDataReader.operatingSystem();
            osInfoWriter.write(osInfo);

            RuntimeInfo runtimeInformation = artemisDataReader.runtimeInformation();
            runtimeInfoWriter.write(runtimeInformation);

            while (duration.canContinue(this) && isRunning()) {
                heapMemoryWriter.write(artemisDataReader.jvmHeapMemory());

                List<JVMMemoryInfo> memoryInfoList = artemisDataReader.jvmMemoryAreas();
                for (JVMMemoryInfo memoryInfo : memoryInfoList) {
                    jvmMemoryAreasWriter.write(memoryInfo);
                }

                try {
                    QueueInfo queueInfoList = artemisDataReader.queueInformation();
                    queueInfoWriter.write(queueInfoList);
                }
                catch (Exception e) {
                    logger.error("Unable to read queue information: {}", e.getMessage(), e);
                }

                Thread.sleep(5000);
            }

            logger.debug("The test has finished and the Artemis inspector is terminating");
            return 0;
        } finally {
            startedEpochMillis = Long.MIN_VALUE;
        }
    }

    public void stop() throws Exception {
        running = false;
    }

    @Override
    public long startedEpochMillis() {
        return startedEpochMillis;
    }
}
