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


Using Maestro
----

Maestro focuses on multi-node deployments by default and deploying it can be a bit tricky
on the first time. However, there are a couple of alternatives to simplify the process:

**Multi-host deployment via Ansible**

There are 3 Ansible roles that can be used to deploy a Maestro test cluster: 
* [ansible-maestro-java](https://github.com/msgqe/ansible-maestro-java): to deploy maestro workers
* [ansible-maestro-broker](https://github.com/msgqe/ansible-maestro-broker): to deploy a Maestro broker
* [ansible-maestro-client](https://github.com/msgqe/ansible-maestro-client): to deploy a Maestro client

These can be used along with other roles to deploy the desired Software Under Test (SUT). 
For example:
* [ansible-amq-broker](https://github.com/msgqe/ansible-amq-broker): to deploy JBoss A-MQ 7 or Apache Artemis single host brokers
* [ansible-broker-clusters](https://github.com/msgqe/ansible-broker-clusters): : to deploy JBoss A-MQ 7 or Apache Artemis clustered brokers
* [ansible-qpid-dispatch](https://github.com/rh-messaging-qe/ansible-qpid-dispatch): to deploy QPid Dispatch Router

This is a much more complex deployment model, but usually desired as it can be made to 
represent real messaging use case scenarios involving multiple hosts.

**Single-host deployment via Docker Compose**

This method is targeted towards development of Maestro and aims to make it simpler to 
deploy and develop local Maestro test clusters. This deployment model is really simple and
it is possible to get started with Maestro testing by running just 3 or 4 commands.

**Note**: although it would be possible to use this model for production testing, this is 
a new feature that needs to be matured.  

This deployment method is documented in greater detail [here](extra/docker/)


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


```
TODO
```

Deploying in Self-Maintained Maven Repository
----

If you maintain your own Maven repository, you can deploy this library using:

```
mvn deploy -DaltDeploymentRepository=libs-snapshot::default::http://hostname:8081/path/to/libs-snapshot-local
```

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
