# Maestro on Kubernetes

Running maestro on kubernetes allows for easy scaling of clients when setting up tests. It also
allows you to load test messaging endpoints internally on the cluster in case you are unable to
expose the service outside of Kubernetes.

In order to isolate the maestro infra from the SUT hosts, all deployments have a node affinity to
nodes labeled 'nodetype=maestroperf'. By adding this label to your nodes, you can control where
maestro is running.

## Deploy broker

The maestro broker can be any broker that supports MQTT. Provided are templates for ActiveMQ and
Mosquitto.

To deploy the Mosquitto broker:

    kubectl apply -f broker/mosquitto-deployment.yaml -f broker/cluster-service.yaml -f broker/external-service.yaml

To deploy the ActiveMQ broker:

    kubectl apply -f broker/activemq-deployment.yaml -f broker/cluster-service.yaml -f broker/external-service.yaml


## Deploy worker

The worker connects to the maestro broker through the service.

To deploy the worker:

    kubectl apply -f worker/

Scale the workers up to the amount of replicas that you need for your test: 

    kubectl scale --replicas=2 -f worker/worker-deployment.yaml

## Running the client

You can run the client outside of your Kubernetes cluster or inside of it. To run it outside,
connect to the broker port using the 'broker-external' service node port.

Running it on the inside has some advantages in that you don't need to have the client binary handy,
and using plain kubernetes tools is possible.

The maestro client is configured using 2 config maps. The test-scripts config map is used to load
the test scripts you wish to select from. This configmap can be created by referencing the directory
containing the scripts on creation:

    kubectl create configmap test-scripts --from-file=../../maestro-test-scripts/src/main/groovy/singlepoint

Each test case is run by creating a configmap with the parameters for the test, and creating the pod
that runs the client and collects the reports. The reports are stored in a pod volume that
you can access through a sidecar container after running the client.

To run a test:

    kubectl apply -f client/testcase-1.yaml
    kubectl apply -f client/client.yaml

The test is finished once the job is marked as complete. If the test fails it will be rerun. You can wait for the job to complete with a command like this:

    until kubectl get pod maestro-client -o jsonpath='{.status.containerStatuses[?(@.name=="client")].state.terminated.reason}' | grep Completed ; do sleep 1; done
    
Once the job is complete, collect the reports from the persistent volume and delete the job :

    kubectl cp -c idle maestro-client:/maestro/reports/ reports/
    kubectl delete pod maestro-client

You can now start a new test case by replacing the client-config configmap with a new test case, and
recreate the pod
