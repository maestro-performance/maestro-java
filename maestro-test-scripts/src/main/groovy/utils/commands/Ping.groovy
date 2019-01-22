/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

@GrabResolver(name='orpiske-bintray', root='https://dl.bintray.com/orpiske/libs-release')
@Grab(group='org.maestro', module='maestro-client', version='1.5.3')

import org.maestro.client.Maestro
import org.maestro.common.client.notes.MaestroNote
import java.util.concurrent.CompletableFuture

/**
 * This example demonstrates how to use a note processor to process
 * replies from the test cluster
 */

/**
 * Defines a processor class
 */

/**
 * Collects the broker URL from the MAESTRO_BROKER environment variable
 */
maestroURL = System.getenv("MAESTRO_BROKER")

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

/**
 * Issue a ping request
 */
CompletableFuture<List<? extends MaestroNote>> pingFuture = maestro.pingRequest()

/**
 * Collect the replies
 */
println "Collecting replies "
List<? extends MaestroNote> replies = pingFuture.get()

/**
 * Use a processor to iterate over the replies
 */
replies.each { MaestroNote note ->
    println "Ping replies: " + note
}

/**
 * Clean shutdown
 */
maestro.stop()


