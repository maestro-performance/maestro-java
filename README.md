Maestro Java: an implementation of the Maestro orchestration API in Java
============


Introduction
----


Building
----
Build Status (devel): [![Build Status](https://travis-ci.org/maestro-performance/maestro-java.svg?branch=devel)](https://travis-ci.org/maestro-performance/maestro-java)

Build Status (master): [![Build Status](https://travis-ci.org/maestro-performance/maestro-java.svg?branch=master)](https://travis-ci.org/maestro-performance/maestro-java)

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

Using Maestro
---- 

Basic Maestro documentation is available [here](extra/doc/Using.md).


Components: Inspectors
---- 

Documentation about the Inspectors is available [here](extra/doc/Inspectors.md).


Components: Exporter (Monitoring)
---- 

Some tips and tricks for monitoring a Maestro test cluster are available [here](extra/doc/Monitoring.md).


Demos
---- 

Some demonstration about using Maestro, focused on the front-end, is available [here](extra/doc/Demos.md).


Development
---- 

Some tips and tricks for developing and debugging Maestro are available [here](extra/doc/Development.md).


Another information source
----
An additional information about Maestro, Maestro Agent, Maestro Inspector and the usage of Maestro for 
the performance testing and analysis can be found in this [thesis](http://www.fit.vutbr.cz/study/DP/DP.php.cs?id=21191&file=t).
