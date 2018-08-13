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

import org.maestro.common.client.notes.MaestroNote;
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
     * Sends a flush request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    @Deprecated
    CompletableFuture<List<? extends MaestroNote>> flushRequest() throws MaestroConnectionException;


    /**
     * Sends a flush request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    @Deprecated
    CompletableFuture<List<? extends MaestroNote>> flushRequest(final String topic) throws MaestroConnectionException;

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
    CompletableFuture<List<? extends MaestroNote>> pingRequest(final String topic) throws MaestroConnectionException;


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setBroker(final String value) throws MaestroConnectionException;


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setBroker(final String topic, final String value) throws MaestroConnectionException;


    /**
     * Sends a set duration request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setDuration(final Object value) throws MaestroException;


    /**
     * Sends a set duration request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setDuration(final String topic, final Object value) throws MaestroException;



    /**
     * Sends a set log level request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    @Deprecated
    CompletableFuture<List<? extends MaestroNote>> setLogLevel(final String value) throws MaestroConnectionException;


    /**
     * Sends a set log level request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setLogLevel(final String topic, final String value) throws MaestroConnectionException;


    /**
     * Sends a set parallel count request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setParallelCount(final int value) throws MaestroConnectionException;


    /**
     * Sends a set parallel count request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setParallelCount(final String topic, final int value) throws MaestroConnectionException;

    /**
     *
     * Sends a set message size request (This one can be used for variable and fixed message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setMessageSize(final String value) throws MaestroConnectionException;


    /**
     * Sends a set message size request (this one can be used for fixed message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setMessageSize(final long value) throws MaestroConnectionException;


    /**
     * Sends a set throttle request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    @Deprecated
    CompletableFuture<List<? extends MaestroNote>> setThrottle(final int value) throws MaestroConnectionException;


    /**
     * Sends a set throttle request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    @Deprecated
    CompletableFuture<List<? extends MaestroNote>> setThrottle(final String topic, final int value) throws MaestroConnectionException;


    /**
     * Sends a set rate request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setRate(final int value) throws MaestroConnectionException;


    /**
     * Sends a set rate request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setRate(final String topic, final int value) throws MaestroConnectionException;


    /**
     * Sends a set fail-condition-latency (FCL) request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> setFCL(final int value) throws MaestroConnectionException;


    /**
     * Sends a start inspector request
     * @param value the name of the inspector to start
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> startInspector(final String value) throws MaestroConnectionException;


    /**
     * Sends a stop inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> stopInspector() throws MaestroConnectionException;


    /**
     * Sends a start sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> startSender() throws MaestroConnectionException;


    /**
     * Sends a stop sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> stopSender() throws MaestroConnectionException;


    /**
     * Sends a start receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> startReceiver() throws MaestroConnectionException;


    /**
     * Sends a stop receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> stopReceiver() throws MaestroConnectionException;


    /**
     * Sends a stats request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> statsRequest() throws MaestroConnectionException;


    /**
     * Sends a halt request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> halt() throws MaestroConnectionException;

    /**
     * Sends a get request
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
     * Sends a agent general execution request
     * @param option An optional numeric option that can be associated w/ the command
     * @param payload An option string payload to be sent along w/ the command
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @return A completable future that can be used to collect the replies for the request
     */
    CompletableFuture<List<? extends MaestroNote>> userCommand(long option, final String payload) throws MaestroConnectionException;
}
