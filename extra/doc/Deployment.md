Maestro Deployment
============

Maestro Architecture and Overview
----

Maestro works by coordinating the work of multiple nodes to generate load and simulate load behavior 
hitting the software under test (SUT). The set represented by the Maestro client, its nodes (aka backends)
and the Maestro cluster is referenced as the Maestro Test Cluster.

The preferred way of running larger deployments of Maestro is within an container-orchestration system such as
[Kubernetes](http://kubernetes.io), [Origin Community Distribution of Kubernetes](http://www.okd.io).

![Maestro Overview](figures/maestro_architecture.png)

Maestro can be used for both low-scale as well as large scale tests. For large scale tests, the recommended
way is to deploy multiple nodes. For small scale and local tests, the docker containers should be sufficient (at a 
small performance penalty cost of ~10% in the max throughput).

The backends generate the load on the SUT using the Protocol Under Test (PUT), which can be any of the supported
protocols.

Maestro Deployment: Using Kubernetes
----

This deployment method is documented in greater detail [here](../kubernetes).

Maestro Deployment: Multi-host deployment via Ansible
----

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

![Maestro Deployment Overview](figures/maestro_deployment.png)

Maestro Deployment: Single-host deployment via Docker Compose
----

This method is targeted towards development of Maestro and aims to make it simpler to 
deploy and develop local Maestro test clusters. This deployment model is really simple and
it is possible to get started with Maestro testing by running just 3 or 4 commands.

**Note**: although it would be possible to use this model for production testing, this is 
a new feature that needs to be matured.  

This deployment method is documented in greater detail [here](../docker-compose/maestro).


Maestro Libraries: Deploying in Self-Maintained Maven Repository
----

If you maintain your own Maven repository, you can deploy this library using:

```
mvn deploy -DaltDeploymentRepository=libs-snapshot::default::http://hostname:8081/path/to/libs-snapshot-local
```

Maestro Deployment: Verifying the Test Cluster
---- 

Run the ping command to check if the test cluster was deployed correctly: 

```
maestro-cli maestro -c ping -m mqtt://host:1883
```

The output should be similar to this:

```
15:42:46,997 Connecting to Maestro Broker
15:42:47,340 Connection to tcp://my.host.com:31883 completed (reconnect = false)
Command                 Name               Host                              Group Name    Member Name
MAESTRO_NOTE_PING       inspector          maestro-inspector-67d8947bdd-k9zr6    all                     
MAESTRO_NOTE_PING       worker             maestro-worker-77c94d6df7-kwpr9    all                     
MAESTRO_NOTE_PING       worker             maestro-worker-77c94d6df7-48ppf    all        
```