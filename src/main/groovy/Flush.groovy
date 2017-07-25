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
import net.orpiske.mpt.maestro.notes.MaestroNote
import net.orpiske.mpt.maestro.notes.MaestroNoteType

@GrabConfig(systemClassLoader=true)

@Grab(group='commons-cli', module='commons-cli', version='1.3.1')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

@Grab(group='org.msgpack', module='msgpack-core', version='0.8.3')

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

brokerURL = System.getenv("MAESTRO_BROKER")

println "Connecting to " + brokerURL
maestro = new Maestro(brokerURL)

maestro.flushRequest()

println "Collecting replies"
List<MaestroNote> replies = maestro.collect()

for (int i = 0; i < 10; i++) {
    if (replies != null && replies.size() > 0) {
        break
    }

    println "Waiting for replies ..."
    Thread.sleep(1)
    replies = maestro.collect()
}

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
}

maestro.stop()


