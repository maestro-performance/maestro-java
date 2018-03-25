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

package org.maestro.client.exchange;

import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.client.notes.MaestroNote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class MaestroCollector extends AbstractMaestroPeer<MaestroNote> {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollector.class);
    private boolean running = true;

    private final List<MaestroNote> collected = Collections.synchronizedList(new LinkedList<MaestroNote>());

    public MaestroCollector(final String url) throws MaestroConnectionException {
        super(url, "maestro-java-collector",MaestroDeserializer::deserialize);
    }


    @Override
    protected void noteArrived(MaestroNote note) {
        collected.add(note);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public synchronized List<MaestroNote> collect() {
        logger.trace("Collecting messages");
        List<MaestroNote> ret = new LinkedList<>(collected);

        collected.clear();

        logger.trace("Number of messages collected: {}", ret.size());
        return ret;
    }

}
