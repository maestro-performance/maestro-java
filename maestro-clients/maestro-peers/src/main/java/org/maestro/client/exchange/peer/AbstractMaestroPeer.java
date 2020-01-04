/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.client.exchange.peer;

import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.client.exchange.MaestroPeer;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Is a base class that implements the basic operations of any peer connect to Maestro (not just senders and receivers).
 * For example, a Collector instance that keeps reading the data from the Maestro broker is an specialization of this
 * class as is the MaestroWorkerManager that handles the requests from the Maestro broker and manages the
 * sender/receiver workers
 * @param <T>
 *
 * TODO: configure LWT
 */
public abstract class AbstractMaestroPeer<T extends MaestroNote> implements MaestroPeer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMaestroPeer.class);

    private ConsumerEndpoint<T> consumerEndpoint;
    private final PeerInfo peerInfo;

    protected AbstractMaestroPeer(final ConsumerEndpoint<T> consumerEndpoint, final PeerInfo peerInfo) throws MaestroConnectionException {
        this.consumerEndpoint = consumerEndpoint;
        this.peerInfo = peerInfo;

        consumerEndpoint.setConsumer(this::noteArrived);
    }


    protected PeerInfo getPeerInfo() {
        return peerInfo;
    }

    public String getId() {
        return consumerEndpoint.getClientId();
    }

    public void connectionLost(Throwable throwable) {
        if (isRunning()) {
            logger.warn("Connection lost");
        }
    }

    public boolean isConnected() {
        return consumerEndpoint.isConnected();
    }


    public void connect() throws MaestroConnectionException {
        consumerEndpoint.connect();
    }

    public void disconnect() throws MaestroConnectionException {
        consumerEndpoint.disconnect();
    }

    public void subscribe(final String[] endpoints) throws MaestroConnectionException {
        consumerEndpoint.subscribe(endpoints);
    }

    /**
     * The entry point for handling Maestro messages
     * @param note the note that arrived
     *
     * @throws MaestroConnectionException for Maestro related errors
     */
    protected abstract void noteArrived(T note) throws MaestroConnectionException;

    public abstract boolean isRunning();


}
