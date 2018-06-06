Maestro: Using
============

Maestro tests are written in Groovy. By default, it already comes with a library of tests 
for single and multi host testing. Additional tests can be written by using the provided 
test API. 

Preparing for the Test Execution
----

The test parameters are exported as environment variables.  It is done this way to facilitate
the integration with continuous integration systems such as Jenkins. As such, the first step 
required for any test is to export the appropriate variables.

The variables to be exported vary if the software under test is deployed in single or in multi
node topologies. This different topologies are referenced as singlepoint and multipoint within
Maestro. Additionally, the variables may vary according to the test - and custom tests may have
their own variables.

Overall, the base set of variables for the tests are: 

**Singlepoint Test Variables**: 

| Variable Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `MAESTRO_BROKER` | `null` | The URL for the Maestro broker |
| `SEND_RECEIVE_URL` | `null` | The URL for the SUT (ie.: ```amqp://testhost/queue```) with options (see below) |
| `MESSAGE_SIZE` | `null` | The [message size](MessageSize.md) |
| `TEST_DURATION` | `null` | The [test duration](TestDuration.md) |
| `LOG_LEVEL` | `null` | Optional log level |
| `MANAGEMENT_INTERFACE` | `null` | The URL for the [management interface](Inspectors.md) |
| `INSPECTOR_NAME` | `null` | The name of the [inspector](Inspectors.md) |


**Multipoint Test Variables**: 

| Variable Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `MAESTRO_BROKER` | `null` | The URL for the Maestro broker |
| `SEND_URL` | `null` | The URL for sending data to the SUT (ie.: ```amqp://testhost/queue```) with options (see below) |
| `RECEIVE_URL` | `null` | The URL for receiving data from the SUT (ie.: ```amqp://testhost/queue```) with options (see below) |
| `MESSAGE_SIZE` | `null` | The [message size](MessageSize.md) |
| `TEST_DURATION` | `null` | The [test duration](TestDuration.md) |
| `LOG_LEVEL` | `null` | Optional log level |
| `MANAGEMENT_INTERFACE` | `null` | The URL for the [management interface](Inspectors.md) |
| `INSPECTOR_NAME` | `null` | The name of the [inspector](Inspectors.md) |


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
  

Running Default Tests
----

Once the test parameters have been adequately set by exporting the test variables. The test can be 
run with one of the following commands:

```
./maestro-cli exec -s ../scripts/singlepoint/IncrementalTest.groovy -d /path/to/save/reports
```

```
cd /path/to/scripts/singlepoint/ && groovy IncrementalTest.groovy /path/to/save/reports
```

The only difference is that the first will use builtin dependencies resulting in a much quicker test
start up time. 


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




