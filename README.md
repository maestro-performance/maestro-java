Maestro Java: an implementation of the Maestro orchestration API in Java
============


Introduction
----


Building
----

```
mvn clean install
```



Using as Command Line Tool
----

Run:

```

```

Using as Library
----

To use this project as library on your project you have to add my personal 
[bintray](https://bintray.com/orpiske/libs-release/) repository to the pom.xml
file:

```
<repositories>
    <repository>
        <id>orpiske-repo</id>
        <url>https://dl.bintray.com/orpiske/libs-release</url>
    </repository>
</repositories>
```

Then, the library can be referenced as: 
```
<dependency>
    <groupId>net.orpiske</groupId>
    <artifactId>maestro-java</artifactId>
    <version>1.2.0</version>
</dependency>
```

The API documentation (javadoc) is available [here](http://www.orpiske.net/files/javadoc/maestro-java-1.2/apidocs/). 
Additional project documentation is available [here](http://www.orpiske.net/files/javadoc/maestro-java-1.2/). 

**Note**: replace version with the latest available version you wish to use.


```
TODO
```

Deploying in Self-Maintained Maven Repository
----

If you maintain your own Maven repository, you can deploy this library using:

```
mvn deploy -DaltDeploymentRepository=libs-snapshot::default::http://hostname:8081/path/to/libs-snapshot-local
```

Samples
----
![Eden](doc/broker-jvm-inspector_eden_memory.png)
![Physical](doc/broker-jvm-inspector_memory.png)
![PermGen](doc/broker-jvm-inspector_pm_memory.png)
![Queue Data](doc/broker-jvm-inspector_queue_data.png)
![Survivor Memory](doc/broker-jvm-inspector_survivor_memory.png)
![Tenured Memory](doc/broker-jvm-inspector_tenured_memory.png)
