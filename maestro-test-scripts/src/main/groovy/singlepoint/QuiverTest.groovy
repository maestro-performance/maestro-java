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

package singlepoint

@GrabConfig(systemClassLoader = true)
@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

@GrabResolver(name='orpiske-bintray', root='https://dl.bintray.com/orpiske/libs-release')
@Grab(group='org.maestro', module='maestro-tests', version='1.5.0-SNAPSHOT')
@Grab(group='org.maestro', module='maestro-reports', version='1.5.0-SNAPSHOT')
@Grab(group='net.orpiske', module='quiver-data-plotter', version='1.0.0')

import org.maestro.client.Maestro
import org.maestro.client.exchange.MaestroTopics
import org.maestro.common.LogConfigurator
import org.maestro.common.Role
import org.maestro.reports.downloaders.BrokerDownloader

import org.maestro.reports.downloaders.ReportsDownloader
import org.maestro.tests.AbstractTestProfile

import net.orpiske.qdp.main.QuiverReportWalker
import net.orpiske.qdp.plot.renderer.IndexRenderer
import org.maestro.tests.cluster.NonAssigningStrategy
import org.maestro.tests.flex.FlexibleTestExecutor
import org.maestro.tests.flex.singlepoint.FlexibleTestProfile

/**
 * This test executes tests via Maestro Agent using Quiver (https://github.com/ssorj/quiver/)
 */
class QuiverExecutor extends FlexibleTestExecutor {
    private Maestro maestro

    QuiverExecutor(Maestro maestro, ReportsDownloader reportsDownloader, AbstractTestProfile testProfile) {
        super(maestro, reportsDownloader, testProfile, new NonAssigningStrategy(maestro))

        this.maestro = maestro
    }

    void startServices() {
        maestro.userCommand(MaestroTopics.peerTopic(Role.AGENT), 0, "rhea")
        // Wait for up to 2 minutes for the test to complete
        Thread.sleep(60*1000*2)
    }

    @Override
    boolean run() {
        return run(0)
    }
}

def plotQuiverFiles(String directory) {
    println "Generating the reports on $directory"

    // Create a walker for the directory where the Quiver files are
    QuiverReportWalker reportWalker = new QuiverReportWalker()

    // Traverse the directory processing the files and plotting them
    File outputDirectory = new File(directory)
    reportWalker.walk(outputDirectory)

    // Generate the HTML reports for the files
    IndexRenderer indexRenderer = new IndexRenderer()
    renderSenderPage(indexRenderer, outputDirectory)
    renderReceiverPage(indexRenderer, outputDirectory)
    renderIndexPage(indexRenderer, outputDirectory)

    // Copy the common assets
    indexRenderer.copyResources(outputDirectory)
}

/**
 * Get the maestro broker URL via the MAESTRO_BROKER environment variable
 */
maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker URL was not given"

    System.exit(1)
}

brokerURL = System.getenv("SEND_RECEIVE_URL")
if (brokerURL == null) {
    println "Error: the send/receive URL was not given"

    System.exit(1)
}

sourceURL = System.getenv("SOURCE_URL")
if (sourceURL == null) {
    println "Warning: the quiver URL was not given. Using default: https://github.com/maestro-performance/maestro-quiver-agent.git"

    sourceURL = "https://github.com/maestro-performance/maestro-quiver-agent.git"
}

logLevel = System.getenv("LOG_LEVEL")
LogConfigurator.configureLogLevel(logLevel)

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = new BrokerDownloader(maestro, args[0])

println "Creating the profile"
FlexibleTestProfile testProfile = new FlexibleTestProfile()

testProfile.setSendReceiveURL(brokerURL)
testProfile.setSourceURL(sourceURL)

println "Creating the executor"
QuiverExecutor executor = new QuiverExecutor(maestro, reportsDownloader, testProfile)

int ret = 0

try {
    println "Running the test"
    if (!executor.run()) {
        ret = 1
    }
} finally {
    println "Stopping the workers on the cluster if they haven't already done so"
    maestro.stop()

    println "Plotting the data"
    plotQuiverFiles(args[0])
}

if (ret == 0) {
    println "Test completed successfully"
}
else {
    println "Test completed with errors"
}
System.exit(ret)
