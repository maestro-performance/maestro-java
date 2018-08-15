Maestro: Inspector
============

Inspectors add additional telemetry support for the SUT. For example, it can be used to measure
the queue(s) length during the test duration, JVM memory usage and other relevant SUT-specific telemetry.

The inspectors are enabled by sending the appropriate Maestro command for the cluster via the 
**setManagementInterface** and **startInspector** commands. The first command sets the URL whereas the second start
the inspector of the given name.

The Inspector continuously sending requests to the SUT every few seconds. This interval can be changed in the Inspector configuration file: 
`~/maestro-inspector/config/maestro-inspector.properties` by value `inspector.sleep.interval` (default: 5s)

The following Figure mapps the whole process of SUT inspecting by Maestro Inspector.

![Inspector Process](figures/inspector_process.png)

Artemis Inspector
----

Is used for Apache Artemis (greater than 2.4.0) and provide support for measuring the queue length, JVM memory usage, 
system details, and others.

**Name**: ArtemisInspector
**URL Format**: ```http://user:password@host:8161/console/jolokia```

AMQP Inspector
----

It is used for Apache Qpid Dispatch Router. It uses the request-response mechanism for collect relative data from AMQP Management which is available on the running Router instance.

Current version of Maestro (1.5.0-SNAPSHOT) collect data about the memory used by Router,
active links and connections (accepted messages, unsettled messages, etc.) and general router 
information such as number of current active connections, neighbor nodes, etc.


**Name**: InterconnectInspector
**URL Format**: ```amqp://host/```

An example of AMQP Inspector start before the test execution is depicted in the following figure.

![Inspector Start](figures/inspector_start.png)