Maestro Java: an implementation of the Maestro orchestration API in Java
============


Introduction
----


Build Status
----
Build Status (devel): [![Build Status](https://travis-ci.org/maestro-performance/maestro-java.svg?branch=devel)](https://travis-ci.org/maestro-performance/maestro-java)

Build Status (master): [![Build Status](https://travis-ci.org/maestro-performance/maestro-java.svg?branch=master)](https://travis-ci.org/maestro-performance/maestro-java)

Codacy Report: [![Codacy Badge](https://api.codacy.com/project/badge/Grade/ddaacf55e38140bb82aa15f02f158164)](https://www.codacy.com/app/orpiske/maestro-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=orpiske/maestro-java&amp;utm_campaign=Badge_Grade)


Deploying Maestro
----

Being a distributed performance testing tool that is opinionated towards large scale performance test execution and 
automation, Maestro focuses on multi-node deployments by default. Nonetheless, is entirely possible to run Maestro on a 
single node and utilities to simplify that are provided by default with the code. 

There are a couple of alternatives to simplify the process or getting a Maestro deployment in place, including Ansible 
playbooks and templates for running Maestro on container orchestration systems such as kubernetes. Please read the 
[Deployment Documentation](extra/doc/Deployment.md) for details about how to deploy Maestro or run it locally.

Using Maestro
---- 

Documentation about using Maestro is available [here](extra/doc/Using.md).


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


Another Information Source
----
An additional information about Maestro, Maestro Agent, Maestro Inspector and the usage of Maestro for 
the performance testing and analysis can be found in this [thesis](http://www.fit.vutbr.cz/study/DP/DP.php.cs?id=21191&file=t).
