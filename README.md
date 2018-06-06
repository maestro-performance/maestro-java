Maestro Java: an implementation of the Maestro orchestration API in Java
============


Introduction
----


Building
----
Build Status: [![Build Status](https://travis-ci.org/maestro-performance/maestro-java.svg?branch=devel)](https://travis-ci.org/maestro-performance/maestro-java)

Codacy Report: [![Codacy Badge](https://api.codacy.com/project/badge/Grade/ddaacf55e38140bb82aa15f02f158164)](https://www.codacy.com/app/orpiske/maestro-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=orpiske/maestro-java&amp;utm_campaign=Badge_Grade)


Local build:
```
mvn clean install
```

Packaging for release:

```
mvn -PDelivery clean package
```


Deploying Maestro
----

Maestro focuses on multi-node deployments by default and deploying it can be a bit tricky
on the first time. However, there are a couple of alternatives to simplify the process, 
including Ansible playbooks as docker containers. Please read the 
[Deployment Documentation](extra/doc/Deployment.md) for details about how to deploy Maestro.


Using as Library
----

**Note**: this is not important unless you are developing Maestro performance tests.


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
    <groupId>org.maestro</groupId>
    <artifactId>maestro-java</artifactId>
    <version>1.2.0</version>
</dependency>
```

There are multiple components and it is possible to choose only the desired one: 

* maestro-client
* maestro-common
* maestro-contrib
* maestro-exporter
* maestro-reports
* maestro-tests
* maestro-worker

The API documentation (javadoc) is available [here](http://www.orpiske.net/files/javadoc/maestro-java-1.2/apidocs/). 
Additional project documentation is available [here](http://www.orpiske.net/files/javadoc/maestro-java-1.2/). 

**Note**: replace version with the latest available version you wish to use.



Usage - Runtime Parameters and Message Customization
----

The following parameters can be set for the JMS worker/client:

| Parameter Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `protocol` | `AMQP` | The underlying messaging protocol to use (one of AMQP, OPENWIRE, ARTEMIS, RABBITAMQP) |
| `type` | queue | Destination type ('queue' or 'topic') |
| `ttl` | 5000 | Time to live. |
| `durable` | true | Durable flag for the message |
| `priority` | null | Message priority |
| `limitDestinations` | `1` | Distributes the load in a fixed number of queues (<= number of connections) |


Components: Inspectors
---- 

Documentation about the Inspectors is available [here](extra/doc/Inspectors.md).


Demos
---- 

Some demonstration about using Maestro, focused on the front-end, is available [here](extra/doc/Demos.md).