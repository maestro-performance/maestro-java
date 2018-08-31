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
@Grab(group='org.maestro', module='maestro-client', version='1.5.0-SNAPSHOT')

import org.maestro.client.Maestro
import org.maestro.client.notes.GetResponse
import org.maestro.common.LogConfigurator
import org.maestro.common.client.notes.MaestroNote
import org.maestro.reports.downloaders.BrokerDownloader

import java.util.concurrent.CompletableFuture

/**
 * This sample script shows how to use the Maestro client API to send a flush request to
 * the test cluster
 */

LogConfigurator.debug()
/**
 * Get the maestro broker URL via the MAESTRO_BROKER environment variable
 */
maestroURL = System.getenv("MAESTRO_BROKER")

locationType = System.getenv("LOCATION_TYPE")

/**
 * Connects to the Maestro broker
 */
println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)


/**
 * First, register available data servers on the cluster
 */

CompletableFuture<List<? extends MaestroNote>> future = maestro.getDataServer()

/**
 * Collect any available response
 */
println "Collecting replies"
List<MaestroNote> replies = future.get()

BrokerDownloader downloader = new BrokerDownloader(maestro, args[0])

/**
 * Process any response given. There may be none if no peers are attached to the
 * broker
 */
println "Processing " + replies.size() + " replies"

if (locationType == null || locationType == "success") {

    downloader.getOrganizer().setResultType("success")

    for (MaestroNote note : replies) {
        if (note instanceof GetResponse) {
            downloader.downloadLastSuccessful(note.getId(), note.getPeerInfo())
        }
    }
}
else {
    downloader.getOrganizer().setResultType("failed")

    for (MaestroNote note : replies) {
        if (note instanceof GetResponse) {
            downloader.downloadLastSuccessful(note.getId(), note.getPeerInfo())
        }
    }
}

Thread.sleep(120000)


/**
 * Stops the client
 */
maestro.stop()


