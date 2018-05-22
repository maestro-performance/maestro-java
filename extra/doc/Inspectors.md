Maestro: Demos
============

Inspectors add additional telemetry support for the SUT. For example, it can be used to measure
the queue(s) length during the test duration, JVM memory usage and other relevant SUT-specific telemetry.

The inspectors are enabled by sending the appropriate Maestro command for the cluster via the 
setManagementInterface and startInspector commands. The first command sets the URL whereas the second start
the inspector of the given name.

Artemis Inspector
----

Is used for Apache Artemis (greater than 2.4.0) and provide support for measuring the queue length, JVM memory usage, 
system details, and others. 

Name: ArtemisInspector

Interconnect Inspector
----

It is used for Apache Qpid Dispatch Router.

Name: InterconnectInspector