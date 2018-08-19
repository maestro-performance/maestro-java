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

package org.maestro.common.client;

import org.maestro.common.Role;
import org.maestro.common.agent.Source;
import org.maestro.common.agent.UserCommandData;
import org.maestro.common.client.notes.DrainOptions;
import org.maestro.common.client.notes.LocationType;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.WorkerStartOptions;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface MaestroRequester {

    /**
     * Stops maestro
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void stop() throws MaestroConnectionException;

    /**
     * Sends a ping request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> pingRequest() throws MaestroConnectionException;

    /**
     * Sends a ping request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> pingRequest(String topic) throws MaestroConnectionException;


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setBroker(String topic, String value) throws MaestroConnectionException;


    /**
     * Sends a set duration request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setDuration(String topic, final Object value) throws MaestroException;

    /**
     * Sends a set parallel count request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setParallelCount(String topic, final int value) throws MaestroConnectionException;

    /**
     *
     * Sends a set message size request (This one can be used for variable and fixed message sizes)
     * @param topic the topic to send the request to
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setMessageSize(String topic, String value) throws MaestroConnectionException;


    /**
     * Sends a set message size request (this one can be used for fixed message sizes)
     * @param topic the topic to send the request to
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setMessageSize(String topic, final long value) throws MaestroConnectionException;


    /**
     * Sends a set rate request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setRate(String topic, final int value) throws MaestroConnectionException;


    /**
     * Sends a set fail-condition-latency (FCL) request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setFCL(String topic, final int value) throws MaestroConnectionException;


    /**
     * Sends a start inspector request
     * @param value the name of the inspector to start
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> startInspector(String value) throws MaestroConnectionException;


    /**
     * Sends a stop inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> stopInspector() throws MaestroConnectionException;

    /**
     * Sends a stop inspector request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> stopInspector(String topic) throws MaestroConnectionException;


    /**
     * Sends a start worker request
     * @param topic the topic to send the request to
     * @param options the worker startup options
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> startWorker(String topic, final WorkerStartOptions options)
            throws MaestroConnectionException;


    /**
     * Sends a stop worker request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> stopWorker(String topic) throws MaestroConnectionException;


    /**
     * Stops everything running on the test cluster
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> stopAll();

    /**
     * Sends a stats request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> statsRequest() throws MaestroConnectionException;

    /**
     * Sends a stats request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> statsRequest(String topic) throws MaestroConnectionException;


    /**
     * Sends a halt request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> halt() throws MaestroConnectionException;

    /**
     * Sends a halt request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> halt(final String topic);

    /**
     * Sends a get data server request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> getDataServer() throws MaestroConnectionException;

    /**
     * Sends a start agent request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> startAgent() throws MaestroConnectionException;

    /**
     * Sends a stop agent request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> stopAgent() throws MaestroConnectionException;

    /**
     * Sends a user command request
     *
     * @param topic the topic to send the request to
     * @param userCommandData the user command data
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> userCommand(String topic, final UserCommandData userCommandData) throws MaestroConnectionException;


    /**
     * Sets the management interface URL
     * @param topic the topic to send the request to
     * @param value The management interface URL
     * @throws MaestroException if unable to send the MQTT request
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> setManagementInterface(String topic, String value);

    /**
     * Sends a source request to the agent (which causes it to download the given source)
     * @param topic the topic to send the request to
     * @param source the extension point source
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> sourceRequest(final String topic, final Source source) throws MaestroConnectionException;


    /**
     * Sends a log request
     * @param topic the topic to send the request to
     * @param locationType The location type
     * @param typeName The optional type name (mandatory if the location type is ANY)
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void logRequest(final String topic, final LocationType locationType, final String typeName) throws MaestroConnectionException;

    /**
     * Issues a drain request
     * @param topic the topic to send the request to
     * @param drainOptions drain options
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> drainRequest(final String topic, final DrainOptions drainOptions);


    /**
     * Assign a role to one or more peers
     * @param topic the topic to send the request to
     * @param role The role to assign
     * @throws MaestroException if unable to send the MQTT request
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> roleAssign(final String topic, final Role role) throws MaestroConnectionException;


    /**
     * Unassign a role to one or more peers
     * @param topic the topic to send the request to
     * @throws MaestroException if unable to send the MQTT request
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> roleUnassign(final String topic) throws MaestroConnectionException;


    /**
     * Waits for the drain notifications
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> waitForDrain();


    /**
     * Waits for notifications
     * @param expect how many notifications to expect
     * @return A completable future
     */
    CompletableFuture<List<? extends MaestroNote>> waitForNotifications(int expect);
}
