/*
 *  Copyright ${YEAR} ${USER}
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
import net.orpiske.mpt.maestro.notes.MaestroCommand
import net.orpiske.mpt.maestro.notes.MaestroNote
import net.orpiske.mpt.maestro.notes.MaestroNoteType
import net.orpiske.mpt.maestro.notes.MaestroResponse
import net.orpiske.mpt.maestro.notes.PingResponse

@GrabConfig(systemClassLoader=true)

@Grab(group='commons-cli', module='commons-cli', version='1.3.1')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

@Grab(group='org.msgpack', module='msgpack-core', version='0.8.3')

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

brokerURL = System.getenv("MAESTRO_BROKER")

println "Connecting to " + brokerURL
maestro = new Maestro(brokerURL)

maestro.pingRequest()

println "Collecting replies "
List<MaestroNote> replies = maestro.collect(1000, 10)


println "Processing " + replies.size() + " replies"
replies.each { MaestroNote note ->
    switch (note.getNoteType()) {
        case MaestroNoteType.MAESTRO_TYPE_RESPONSE:
            print "Received response for "
            break

        case MaestroNoteType.MAESTRO_TYPE_REQUEST:
            print "Received request for "
            break
        case MaestroNoteType.MAESTRO_TYPE_NOTIFICATION:
            print "Received notification for "
            break;
    }

    println note.getMaestroCommand()

    if (note.getNoteType() == MaestroNoteType.MAESTRO_TYPE_RESPONSE) {
        println "ID: " + ((MaestroResponse) note).getId();
        println "Name: " + ((MaestroResponse) note).getName();
    }

    if (note.getMaestroCommand() == MaestroCommand.MAESTRO_NOTE_PING) {
        println "Elapsed time: " + ((PingResponse) note).getElapsed()
    }
}

maestro.stop()


