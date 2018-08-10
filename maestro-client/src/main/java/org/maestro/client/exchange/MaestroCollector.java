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

import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class MaestroCollector extends AbstractMaestroPeer<MaestroNote> {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollector.class);
    private volatile boolean running = true;

    private final Queue<MaestroNote> collected = new ConcurrentLinkedQueue<>();
    private final List<MaestroNoteCallback> callbacks = new LinkedList<>();
    private final List<MaestroMonitor> monitored = new LinkedList<>();

    public MaestroCollector(final String url) throws MaestroConnectionException {
        super(url, "maestro-java-collector",MaestroDeserializer::deserialize);
    }

    @Override
    protected void noteArrived(MaestroNote note) {
        for (MaestroNoteCallback callback : callbacks) {
            callback.call(note);
        }

        collected.add(note);

        if (logger.isTraceEnabled()) {
            logger.trace("Message arrived waking up {} threads", monitored.size());
        }

        monitored.forEach(monitor -> { synchronized(monitor) { monitor.notify(); } } );
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public synchronized void clear() {
        collected.clear();
    }

    public synchronized List<MaestroNote> collect(Predicate<? super MaestroNote> predicate) {
        logger.trace("Collecting messages");
        List<MaestroNote> ret = collected.stream()
                .filter(predicate)
                .collect(Collectors.toList());

        collected.removeIf(predicate);

        logger.trace("Number of messages collected: {}", ret.size());
        return ret;
    }

    public synchronized void addCallback(MaestroNoteCallback callback) {
        callbacks.add(callback);
    }

    public synchronized void monitor(final MaestroMonitor monitor) {
        monitored.add(monitor);
    }

    public synchronized void remove(final MaestroMonitor monitor) {
        monitored.remove(monitor);
    }
}
