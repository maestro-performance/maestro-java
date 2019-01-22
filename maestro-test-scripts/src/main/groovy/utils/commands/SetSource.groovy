/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.commands

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

@GrabResolver(name='orpiske-bintray', root='https://dl.bintray.com/orpiske/libs-release')
@Grab(group='org.maestro', module='maestro-tests', version='1.5.3')

import org.maestro.client.Maestro
import org.maestro.client.exchange.MaestroTopics
import org.maestro.common.Role
import org.maestro.common.client.notes.MaestroNote

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


/**
 * Another example: a simple use case of the higher level maestro client
 */


/**
 * Get the maestro broker URL via the MAESTRO_BROKER environment variable
 */
maestroURL = System.getenv("MAESTRO_BROKER")
sourceURL = System.getenv("SOURCE_URL")
branch = System.getenv("BRANCH")



println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

/**
 * Sends a stop command to all the test cluster
 */
println "Sending the source command"
CompletableFuture<List<? extends MaestroNote>> sourceFuture = maestro.sourceRequest(MaestroTopics.peerTopic(Role.AGENT),
        sourceURL, branch)


println "Collecting the replies"
List<? extends MaestroNote> sourceReplies = sourceFuture.get(12000, TimeUnit.MILLISECONDS)
println "Number of source replies: " + sourceReplies.size()

println "Sending ping ..."
CompletableFuture<List<? extends MaestroNote>> pingFuture = maestro.pingRequest()

int peers = pingFuture.get(2000, TimeUnit.MILLISECONDS).size()
println "Number of ping replies: " + peers

println "Stopping the agent"
maestro.stopAgent()

List<MaestroNote> replies = maestro.waitForNotifications(peers)
println "Processing " + replies.size() + " replies"
replies.each { MaestroNote note ->
    println "Available responses on the broker: " + note
}

println "Stopping maestro"
maestro.stop()

