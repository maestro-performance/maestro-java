Maestro: Development
============

A sample development workflow for Maestro looks like this: launch the basic infrastructure for running Maestro and 
launching the peers/nodes that you need to execute your development.

Running the Infrastructure Locally
----

1. Run the infrastructure using the docker compose locally on your computer: 
```docker-compose -f docker-devel-compose.yml up```

The composer images will expose the management interfaces for the Maestro broker and the SUT. If needed, they can 
be accessed via the following URLs:

* URL for the Maestro broker: [http://localhost:18161/](http://localhost:18161/)
* URL for the SUT broker: [http://localhost:8161/](http://localhost:8161/)


Running the Workers Locally
----

To run the workers locally might depend on the IDE, or if you are using the CLI. The usual configuration for the CLI
involves the following steps:

1: Adjust the maestro home property: 

```-Dorg.maestro.home=${project.location}/maestro-java/maestro-worker/src/main/resources/```

**Note**: Replace the pseudo-variable for the project location (`${project.location}`) with the actual directory for the project (ie: /path/to/the/project)

2 Adjust the program arguments so that the workers connect to the local infrastructure:

* Receiver:
 
```-m mqtt://localhost:1884 -r receiver -H localhost -w org.maestro.worker.jms.JMSReceiverWorker -l /storage/tmp/maestro-java/worker/receiver```

* Sender:
 
```-m mqtt://localhost:1884 -r sender -H  localhost -w org.maestro.worker.jms.JMSSenderWorker -l /storage/tmp/maestro-java/worker/sender```



Running a Client
----

This might also depend on the IDE and CLI. The overall steps are:

1. Adjust the maestro home property for the client: 

```-Dorg.maestro.home=${project.location}/maestro-java/maestro-cli/src/main/resources/```

**Note**: Replace the pseudo-variable for the project location (`${project.location}`) with the actual directory for the project (ie: /path/to/the/project).

2. Adjust the command line for the maestro client:

```exec -d ${report.directory} -s ${project.location}/maestro-java/maestro-test-scripts/src/main/groovy/singlepoint/FixedRateTest.groovy```

**Note 1**: Replace the pseudo-variable for the report directory (`${report.directory}`) with the desired location for saving the reports.

**Note 2**: Replace the pseudo-variable for the project location (`${project.location}`) with the actual directory for the project (ie: /path/to/the/project).

3. Set the environment variables for the test: 

```
SEND_RECEIVE_URL=amqp://localhost:5672/test.performance.queue?protocol=AMQP&limitDestinations=5
# If needed 
# INSPECTOR_NAME=ArtemisInspector
MAESTRO_BROKER=mqtt://localhost:1884

# If needed
# MANAGEMENT_INTERFACE=http://admin:admin@localhost:8161/console/jolokia
MESSAGE_SIZE=~200
PARALLEL_COUNT=5
RATE=0
TEST_DURATION=3m
```


Run Configurations for IntelliJ
----

Some tips and tricks for developing and debugging Maestro are available [here](development/runConfigurations). To use
those, you can copy all the XML files to your ```${project.dir}/.idea/runConfigurations``` directory


Remote Debugging 
----

To enable remote debugging, export the variable MAESTRO_DEBUG and set it to "y". The test for the variable is case 
sensitive. The debug port is set to 8000 for all components.

Version bump
----

Run the following to bump the versions:

```
mvn versions:set -DnewVersion=new_version
```

And then the following to accept the changes:

```
mvn versions:commit
```
