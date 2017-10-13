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

import net.orpiske.mpt.maestro.Maestro
import net.orpiske.mpt.reports.ReportDirProcessor
import net.orpiske.mpt.reports.ReportGenerator
import net.orpiske.mpt.reports.ReportsDownloader
import net.orpiske.mpt.test.incremental.IncrementalTestExecutor
import net.orpiske.mpt.test.incremental.IncrementalTestProfile
import net.orpiske.mpt.utils.LogConfigurator
import net.orpiske.mpt.utils.MessageSize
import net.orpiske.mpt.utils.TestDuration

@GrabConfig(systemClassLoader=true)

@Grab(group='commons-cli', module='commons-cli', version='1.3.1')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

@Grab(group='org.msgpack', module='msgpack-core', version='0.8.3')

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

maestroURL = System.getenv("MAESTRO_BROKER")
brokerURL = System.getenv("BROKER_URL")

LogConfigurator.debug()

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = new ReportsDownloader("/tmp/maestro");

IncrementalTestProfile testProfile = new IncrementalTestProfile();

testProfile.setBrokerURL(brokerURL)
testProfile.setInitialRate(500);
testProfile.setDuration(TestDuration.newInstance("30s"));
testProfile.setMessageSize(MessageSize.variable(256));
testProfile.setMaximumLatency(200)

IncrementalTestExecutor testExecutor = new IncrementalTestExecutor(maestro, reportsDownloader, testProfile)

if (!testExecutor.run()) {
    maestro.stop()

    ReportGenerator.generate("/tmp/mpt/groovy")
    return 1
}

maestro.stop()
ReportGenerator.generate("/tmp/maestro")
return 0


