Maestro Java: an implementation of the Maestro orchestration API in Java
============

Introduction
----

This guide describes how to get Maestro running on a container orchestration suite such as [Kubernetes](http://kubernetes.io),
[Origin Community Distribution of Kubernetes](http://www.okd.io) or [Red Hat OpenShift](https://www.openshift.com/).

One of the benefits of running maestro on kubernetes, is that it allows easy scaling of clients when setting up tests.
It also allows you to load test messaging endpoints internally on the cluster in case you are unable to expose the
service outside of Kubernetes.

**Note 1**: within this guide, Kubernetes is used as the reference implementation, but OKD and OpenShift are equally
supported (and, in fact, OpenShift is the recommended Kubernetes distribution for running Maestro).


**Note 2**: these templates are fully functional but provided as reference implementation. Ultimately, specific details
of the infrastructure running Maestro may apply and changes may be required in such cases.

Preparation
----

In order to isolate the maestro infra and workers from the SUT hosts, all deployments should have a node affinity so 
that you can control where Maestro components are running. 

The affinity is defined by applying the `maestro-node-role` label to a node and setting it to one of the following 
predefined values;

* worker
* infra

Nodes with the `worker` label have an affinity towards worker type components. These are: the Maestro Worker and the 
Maestro Agent. Nodes with the `infra` label have an affinity towards infra type components. These are low resource 
intensive components that provide support for the test execution or the infrastructure required for running Maestro. 
These components are: the Maestro Inspector, Maestro Reports and the Maestro Broker.    

Nodes can be labeled like this:

```
kubectl label node my-node-01.corp.com maestro-node-role=infra
```


Deploy broker
----

The maestro broker can be any broker that supports MQTT. Provided are templates for ActiveMQ and
Mosquitto.

**Mosquitto**

To deploy the Mosquitto broker:

```
kubectl apply -f broker/mosquitto-deployment.yaml -f broker/cluster-service.yaml -f broker/external-service.yaml
```

To deploy the ActiveMQ broker:

```
kubectl apply -f broker/activemq-deployment.yaml -f broker/cluster-service.yaml -f broker/external-service.yaml
```

Deploy the Reports Tool
----

To deploy the reports, first we create volume claim to store the data permanently:

```
kubectl apply -f reports/reports-data-pvc.yaml
```

Then we create the services, the deployment and expose the services:

```
kubectl apply -f reports/reports-service.yaml -f reports/reports-deployment.yaml
kubectl expose -f reports/reports-service.yaml --hostname=my.hostname.com
```

**Note**: be sure to replace `my.hostname.com` with the correct hostname for the reports server.

Deploy worker
----

The worker connects to the maestro broker through the broker service.

To deploy the worker:

```
kubectl apply -f worker/worker-deployment.yaml
```

Scale the workers up to the amount of replicas that you need for your test: 

```
kubectl scale deployment --replicas=2 maestro-worker
```

Deploy inspector
----

To deploy the inspector:

```
kubectl apply -f inspector/inspector-deployment.yaml
``` 

**Note**: the inspector is as an experimental component which may affect the stability of the SUT.

Deploy agent
----

To deploy the agent:

```
kubectl apply -f agent/agent-deployment.yaml
``` 


Running the client
----

**Running Outside Kubernetes**

You can run the client outside of your Kubernetes cluster or inside of it. To run it outside, connect to the broker 
port using the 'broker-external' service node port. The default port is 31883 for external broker service, but it 
can be changed on the broker templates.

Running the client outside Kubernetes is the recommended way.   

**Running Inside Kubernetes**

Running it on the inside has some advantages in that you don't need to have the client binary installed and using plain 
kubernetes tools is possible.

The maestro client is configured using 2 config maps. The test-scripts config map is used to load
the test scripts you wish to select from. This configmap can be created by referencing the directory
containing the scripts on creation:

```
kubectl create configmap test-scripts --from-file=../../maestro-test-scripts/src/main/groovy/singlepoint
```

Each test case is run by creating a configmap with the parameters for the test, and creating the pod
that runs the client and collects the reports. The reports are stored in a pod volume that
you can access through a sidecar container after running the client.

To run a test:

```
kubectl apply -f client/testcase-1.yaml
kubectl apply -f client/client.yaml
```

The test is finished once the job is marked as complete. If the test fails it will be rerun. You can wait for the job to complete with a command like this:

```
until kubectl get pod maestro-client -o jsonpath='{.status.containerStatuses[?(@.name=="client")].state.terminated.reason}' | grep Completed ; do sleep 1; done
```
    

You can now start a new test case by replacing the client-config configmap with a new test case, and recreating the pod.
