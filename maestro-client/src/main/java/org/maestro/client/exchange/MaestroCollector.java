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
import org.maestro.client.exchange.support.CollectorPeer;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

/**
 * An specialized peer that is used on the front-end side of the code
 * to collect messages published on front-end related topics
 */
public class MaestroCollector extends AbstractMaestroPeer<MaestroNote> {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollector.class);
    private volatile boolean running = true;

    private final BlockingQueue<MaestroNote> collected = new LinkedBlockingQueue<>();
    private final List<MaestroNoteCallback> callbacks = new LinkedList<>();

    // To prevent throwing ConcurrentModificationException when iterating the list
    private final List<MaestroMonitor> monitored = new CopyOnWriteArrayList<>();

    /**
     * Constructor
     * @param url the URL to the Maestro broker
     * @throws MaestroConnectionException if unable to connect
     */
    public MaestroCollector(final String url) throws MaestroConnectionException {
        super(url, new CollectorPeer(), MaestroDeserializer::deserialize);

        addCallback(new IgnoreCallback());
    }


    @Override
    protected void noteArrived(MaestroNote note) {
        for (MaestroNoteCallback callback : callbacks) {
            if (!callback.call(note)) {
                return;
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Message {} arrived. Running awake check for {} monitors", note, monitored.size());
        }

        monitored.forEach(monitor -> awakeCheck(note, monitor));

        if (!collected.add(note)) {
            logger.error("Unable to add the note {} to the collected notes cache", note);
        }
    }

    private void awakeCheck(final MaestroNote note, final MaestroMonitor monitor) {
        if (monitor.shouldAwake(note)) {
            logger.trace("Predicate check successful for note {}", note);
            monitor.doUnlock();
        }
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
        List<MaestroNote> ret = new ArrayList<>(collected.size());

        for (MaestroNote note : collected) {
            if (predicate.test(note)) {
                logger.trace("Collecting message {} that matched a predicate", note);
                if (!collected.remove(note)) {
                    logger.error("Removing message {} failed", note);
                }
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
    public void monitor(final MaestroMonitor monitor) {
        monitored.add(monitor);
    }

    /**
     * Removes a monitor
     * @param monitor the monitor to remove
     */
    public void remove(final MaestroMonitor monitor) {
        monitored.remove(monitor);
    }
}
