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

package net.orpiske.mpt.main;

import net.orpiske.mpt.maestro.MaestroClient;
import net.orpiske.mpt.maestro.MaestroCommand;
import net.orpiske.mpt.maestro.MaestroNote;
import net.orpiske.mpt.maestro.MaestroNoteType;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class Maestro {
    private String url;
    private MaestroClient maestroClient = null;

    public Maestro(final String url) throws MqttException {
        this.url = url;

        maestroClient = new MaestroClient(url);

        maestroClient.connect();
    }

    public void stop() throws MqttException {
        maestroClient.disconnect();
    }

    public void flush() throws MqttException, IOException {
        MaestroNote maestroNote = new MaestroNote();

        maestroNote.setNoteType(MaestroNoteType.MAESTRO_TYPE_REQUEST);
        maestroNote.setMaestroCommand(MaestroCommand.MAESTRO_NOTE_FLUSH);

        maestroClient.publish("/mpt/daemon", maestroNote);
    }
}
