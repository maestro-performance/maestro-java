Maestro: Using
============

At the end of the tests, you can use the CLI to generate the reports. Some customization is also possible.

Customizing the Reports
----

It is possible to include SUT node information when generating the reports. To do so, you have to create a properties
file containing the SUT node information in the following format:

```
sutJvmMaxMemory=4542955520
sutJvmVersion=25.171-b11
sutJavaHome=/usr/lib/jvm/java-8-openjdk-amd64/jre
sutOperatingSystemArch=amd64
sutSystemCpuCount=4
sutOperatingSystemVersion=4.16.0-041600-generic
sutJvmName=OpenJDK 64-Bit Server VM
sutJavaVersion=1.8.0_172
sutOperatingSystemName=Linux
sutSystemInfo=true
```

They represent, in order: JVM max memory used by the SUT, JVM version, java home, operating system architecture, CPU 
count, number of CPU/cores availables, operating system version, JVM name, operating system name. 
The last property, ```sutSystemInfo```, controls whether or not to display the information on the reports. It must 
always be set to true if you want this information included on the reports. 

To include this information on the reports, pass the following option to Maestro CLI when generating the reports: 

`--with-sut-node-properties-from /path/to/sut-node.properties` 