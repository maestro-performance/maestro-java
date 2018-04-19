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

package org.maestro.common.client;


import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;

/**
 * A generic Maestro client that can be used for publishing or receiving messages (usually for publishing)
 */
public interface MaestroClient {

    /**
     * Connect to the maestro broker
     * @throws MaestroConnectionException if unable to connect to the broker
     */
    void connect() throws MaestroConnectionException;


    /**
     * Disconnects from the maestro broker
     * @throws MaestroConnectionException if failed to disconnect cleanly (should be safe to ignore in most cases)
     */
    void disconnect() throws MaestroConnectionException;

    /**
     * Publishes a message in the broker
     *
     * @param topic the topic to publish the message
     * @param note  the maestro note to publish
     * @throws MaestroConnectionException if failed to publish the message
     * @throws MalformedNoteException     in case of other I/O errors
     */
    void publish(final String topic, final MaestroNote note) throws MalformedNoteException, MaestroConnectionException;



}
