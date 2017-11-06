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

package net.orpiske.mpt.maestro.client;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.exceptions.MalformedNoteException;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MaestroCollector extends AbstractMaestroPeer {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollector.class);


    private List<MaestroNote> collected = Collections.synchronizedList(new LinkedList<MaestroNote>());

    public MaestroCollector(final String url) throws MaestroConnectionException {
        super(url, "maestro-java-collector");
    }


    @Override
    protected void messageArrived(MaestroNote note) {
        collected.add(note);
    }

    public synchronized List<MaestroNote> collect() {
        List<MaestroNote> ret = new LinkedList<MaestroNote>(collected);

        collected.clear();

        return ret;
    }

}
