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

package org.maestro.client.exchange.collector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.maestro.client.Collector;
import org.maestro.common.client.callback.MaestroNoteCallback;
import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.IgnoreCallback;
import org.maestro.client.exchange.MaestroMonitor;
import org.maestro.client.exchange.peer.AbstractMaestroPeer;
import org.maestro.client.exchange.support.CollectorPeer;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An specialized peer that is used on the front-end side of the code
 * to collect messages published on front-end related topics
 */
public class MaestroCollector extends AbstractMaestroPeer<MaestroNote> implements Collector {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollector.class);
    private volatile boolean running = true;

    private final Queue<MaestroNote> collected = new ConcurrentLinkedQueue<>();
    private final List<MaestroNoteCallback> callbacks = new LinkedList<>();

    // To prevent throwing ConcurrentModificationException when iterating the list
    private final List<MaestroMonitor> monitored = new CopyOnWriteArrayList<>();

    /**
     * Constructor
     * @param endpoint the consumer endpoint
     * @throws MaestroConnectionException if unable to connect
     */
    public MaestroCollector(ConsumerEndpoint<MaestroNote> endpoint) throws MaestroConnectionException {
        super(endpoint, new CollectorPeer());

        addCallback(new IgnoreCallback());
    }


    @Override
    protected void noteArrived(MaestroNote note) {
        for (MaestroNoteCallback callback : callbacks) {
            if (!callback.call(note)) {
                return;
            }
        }

        synchronized (this) {
            collected.add(note);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Message {} arrived waking up {} monitors", note, monitored.size());
        }

        monitored.forEach(monitor -> { if (monitor.shouldAwake(note)) monitor.doUnlock(); } );
    }


    /**
     * Sets the running state for the collector
     * @param running true if running or false otherwise
     */
    void setRunning(boolean running) {
        this.running = running;
    }


    /**
     * Checks the running state of the collector
     * @return true if running or false otherwise
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Clear the collected messages
     */
    public synchronized void clear() {
        collected.clear();
    }


    /**
     * Collect notes matching a given predicate
     * @param predicate the predicate that notes need to match in order to be collected
     * @return A list of collected notes
     */
    public List<MaestroNote> collect(Predicate<? super MaestroNote> predicate) {
        logger.trace("Collecting messages");

        final List<MaestroNote> ret = new ArrayList<>(collected.size());

        for (final MaestroNote note : collected) {
            if (predicate.test(note)) {
                collected.remove(note);
                ret.add(note);
            }
        }

        logger.trace("Number of messages collected: {}", ret.size());
        return ret;
    }


    /**
     * Adds a callback to be executed on note arrival
     * @param callback the callback to execute
     */
    public synchronized void addCallback(MaestroNoteCallback callback) {
        callbacks.add(callback);
    }


    /**
     * Removes a callback executed on note arrival
     * @param callback the callback to remove
     */
    public synchronized void removeCallback(MaestroNoteCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * Adds a monitor for message arrival
     * @param monitor the monitor to add
     */
    public synchronized void monitor(final MaestroMonitor monitor) {
        monitored.add(monitor);
    }

    /**
     * Removes a monitor
     * @param monitor the monitor to remove
     */
    public synchronized void remove(final MaestroMonitor monitor) {
        monitored.remove(monitor);
    }
}
