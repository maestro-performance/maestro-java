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

@GrabConfig(systemClassLoader=true)

@Grab(group='commons-cli', module='commons-cli', version='1.3.1')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')
@Grab(group='org.msgpack', module='msgpack-core', version='0.8.3')

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

@GrabResolver(name='orpiske-bintray', root='https://dl.bintray.com/orpiske/libs-release')
@Grab(group='org.maestro', module='maestro-tests', version='1.3.0-SNAPSHOT')
@Grab(group='org.maestro', module='maestro-reports', version='1.3.0-SNAPSHOT')
@Grab(group='net.orpiske', module='quiver-data-plotter', version='1.0.0')

import org.maestro.client.Maestro
import org.maestro.common.LogConfigurator
import org.maestro.reports.AbstractReportResolver
import org.maestro.reports.ReportsDownloader
import org.maestro.reports.organizer.DefaultOrganizer
import org.maestro.tests.AbstractTestProfile

import net.orpiske.qdp.main.QuiverReportWalker
import net.orpiske.qdp.plot.renderer.IndexRenderer
import org.maestro.tests.flex.FlexibleTestExecutor
import org.maestro.tests.flex.singlepoint.FlexibleTestProfile

import static net.orpiske.qdp.plot.renderer.EasyRender.*

/**
 * This test executes tests via Maestro Agent using Quiver (https://github.com/ssorj/quiver/)
 */
class QuiverExecutor extends FlexibleTestExecutor {
    private Maestro maestro

    QuiverExecutor(Maestro maestro, ReportsDownloader reportsDownloader, AbstractTestProfile testProfile) {
        super(maestro, reportsDownloader, testProfile)

        this.maestro = maestro;
    }

    void startServices() {
        maestro.userCommand(0, "rhea")
    }
}

class QuiverReportResolver extends AbstractReportResolver {
    private static final String[] FILES = ["receiver-snapshots.csv", "receiver-transfers.csv.xz", "sender-summary.json",
                          "receiver-summary.json", "sender-snapshots.csv", "sender-transfers.csv.xz"]

    QuiverReportResolver() {
        super(FILES)
    }

    @Override
    List<String> getFailedFiles(String baseURL) {
        return getTestFiles(baseURL, "quiver");
    }

    @Override
    List<String> getSuccessFiles(String baseURL) {
        return getTestFiles(baseURL, "quiver");
    }

    @Override
    List<String> getTestFiles(String baseURL, String testNum) {
        return listBuilder(baseURL, "quiver");
    }
}

class QuiverOrganizer extends DefaultOrganizer {
    QuiverOrganizer(String baseDir) {
        super(baseDir)
    }

    @Override
    String organize(String address, String hostType) {
        return getBaseDir()
    }
}

def plotQuiverFiles(String directory) {
    println "Generating the reports on $directory"

    // Create a walker for the directory where the Quiver files are
    QuiverReportWalker reportWalker = new QuiverReportWalker();

    // Traverse the directory processing the files and plotting them
    File outputDirectory = new File(directory);
    reportWalker.walk(outputDirectory);

    // Generate the HTML reports for the files
    IndexRenderer indexRenderer = new IndexRenderer();
    renderSenderPage(indexRenderer, outputDirectory);
    renderReceiverPage(indexRenderer, outputDirectory);
    renderIndexPage(indexRenderer, outputDirectory);

    // Copy the common assets
    indexRenderer.copyResources(outputDirectory);
}

/**
 * Get the maestro broker URL via the MAESTRO_BROKER environment variable
 */
maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker URL was not given"

    System.exit(1)
}

brokerURL = System.getenv("BROKER_URL")
if (brokerURL == null) {
    println "Error: the broker URL was not given"

    System.exit(1)
}

sourceURL = System.getenv("SOURCE_URL")
if (sourceURL == null) {
    println "Warning: the quiver URL was not given. Using default: https://github.com/maestro-performance/maestro-quiver-agent.git"

    sourceURL = "https://github.com/maestro-performance/maestro-quiver-agent.git"
}

LogConfigurator.verbose()

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = new ReportsDownloader(new QuiverOrganizer(args[0]))
reportsDownloader.addReportResolver("agent", new QuiverReportResolver())

println "Creating the profile"
FlexibleTestProfile testProfile = new FlexibleTestProfile();

testProfile.setBrokerURL(brokerURL)
testProfile.setSourceURL(sourceURL)

println "Creating the executor"
QuiverExecutor executor = new QuiverExecutor(maestro, reportsDownloader, testProfile)

println "Running the test"
if (!executor.run()) {
    maestro.stop()
    plotQuiverFiles(args[0])

    System.exit(1)
}

maestro.stop()
plotQuiverFiles(args[0])
System.exit(0)
