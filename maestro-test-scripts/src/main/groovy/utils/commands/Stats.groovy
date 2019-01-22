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
import org.maestro.common.client.notes.MaestroNote

import java.util.concurrent.CompletableFuture

/**
 * Another example: a simple use case of the higher level maestro client
 */


/**
 * Get the maestro broker URL via the MAESTRO_BROKER environment variable
 */
maestroURL = System.getenv("MAESTRO_BROKER")


println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

/**
 * Issue the get request
 */
CompletableFuture<List<? extends MaestroNote>> future = maestro.statsRequest()

/**
 * Collect any available response
 */
println "Collecting replies"
List<MaestroNote> replies = future.get()

/**
 * Process any response given. There may be none if no peers are attached to the
 * broker
 */
println "Processing " + replies.size() + " replies"
replies.each { MaestroNote note ->
    println "Available responses on the broker: " + note
}

println "Sent"
maestro.stop()

