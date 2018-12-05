Writing Tests With Maestro: Using
============

This guide extends the [Using](extra/doc/Using.md) guide and focuses on the Maestro API used for testing. 

Introduction
----

Maestro tests are fundamentally written around the [Maestro](http://www.orpiske.net/files/javadoc/maestro-java-1.5/apidocs/org/maestro/client/Maestro.html)
class. The Maestro class wrap the maestro orchestration API and provides an interface that can be used to control the 
test cluster.


High Level Testing Logic
----

The tests have a pre-defined workflow that, no matter how complicated the test, can be summarized as having the
following steps:

![Maestro Overview](figures/test_workflow.png)

Therefore, the secret to write more complicated tests with Maestro is understanding what part of the API to call
for each of those steps. 

* Connect
  * Constructor
* Find Peers and Assign Roles
  * pingRequest
  * roleAssign
  * groupAssign (experimental)
* Set Test Parameters
  * setDuration
  * setParallelCount
  * setMessageSize
  * setMessageSize
  * setRate
  * setFCL
  * setManagementInterface
* Start the Test
  * startInspector
  * startAgent
  * startWorker
* Collect the Results
  * waitForNotifications
  * waitForDrain
* Cleanup
  * roleUnassign
  * groupLeave (experimental)


All of the core API methods return CompletableFutures that can be used to read the responses from the 
test cluster. The CompletableFutures return a list of responses, one for each node on the test cluster. 
The responses are guaranteed to be for the specific request since the API, as of 1.4 and newer, uses a 
correlation identifier to match the responses to the request that originated them. 

Note: the response processing may occurs in two ways depending on the expected amount of responses. If 
a request is known to provide a fixed number of replies, then the collection of those replies will succeed 
if the given amount of replies matches to the expected value. If the amount is unknown, then it will wait 
for matching replies until it considers that the growth of matching replies has staled.


