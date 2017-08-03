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
import net.orpiske.mpt.maestro.client.MaestroNoteProcessor
import net.orpiske.mpt.maestro.notes.MaestroNote
import net.orpiske.mpt.maestro.notes.PingResponse

@GrabConfig(systemClassLoader=true)

@Grab(group='commons-cli', module='commons-cli', version='1.3.1')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

@Grab(group='org.msgpack', module='msgpack-core', version='0.8.3')

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

class PingProcessor extends MaestroNoteProcessor {
    @Override
    protected void processPingResponse(PingResponse note) {
        println  "Elapsed time from " + note.getName() + ": " + note.getElapsed() + " ms"
    }
}

maestroURL = System.getenv("MAESTRO_BROKER")

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

maestro.pingRequest()

println "Collecting replies "
List<MaestroNote> replies = maestro.collect(1000, 10)

(new PingProcessor()).process(replies)

maestro.stop()


