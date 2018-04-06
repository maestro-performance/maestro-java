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

import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;

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
     */
    void flushRequest() throws MaestroConnectionException;


    /**
     * Sends a flush request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void flushRequest(final String topic) throws MaestroConnectionException;

    /**
     * Sends a ping request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void pingRequest() throws MaestroConnectionException;

    /**
     * Sends a ping request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void pingRequest(final String topic) throws MaestroConnectionException;


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setBroker(final String value) throws MaestroConnectionException;


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setBroker(final String topic, final String value) throws MaestroConnectionException;


    /**
     * Sends a set duration request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setDuration(final Object value) throws MaestroException;


    /**
     * Sends a set duration request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setDuration(final String topic, final Object value) throws MaestroException;



    /**
     * Sends a set log level request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setLogLevel(final String value) throws MaestroConnectionException;


    /**
     * Sends a set log level request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setLogLevel(final String topic, final String value) throws MaestroConnectionException;


    /**
     * Sends a set parallel count request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setParallelCount(final int value) throws MaestroConnectionException;


    /**
     * Sends a set parallel count request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setParallelCount(final String topic, final int value) throws MaestroConnectionException;

    /**
     *
     * Sends a set message size request (This one can be used for variable and fixed message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setMessageSize(final String value) throws MaestroConnectionException;


    /**
     * Sends a set message size request (this one can be used for fixed message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setMessageSize(final long value) throws MaestroConnectionException;


    /**
     * Sends a set throttle request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setThrottle(final int value) throws MaestroConnectionException;


    /**
     * Sends a set throttle request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setThrottle(final String topic, final int value) throws MaestroConnectionException;


    /**
     * Sends a set rate request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setRate(final int value) throws MaestroConnectionException;


    /**
     * Sends a set rate request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setRate(final String topic, final int value) throws MaestroConnectionException;


    /**
     * Sends a set fail-condition-latency (FCL) request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void setFCL(final int value) throws MaestroConnectionException;


    /**
     * Sends a start inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void startInspector() throws MaestroConnectionException;


    /**
     * Sends a stop inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void stopInspector() throws MaestroConnectionException;


    /**
     * Sends a start sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void startSender() throws MaestroConnectionException;


    /**
     * Sends a stop sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void stopSender() throws MaestroConnectionException;


    /**
     * Sends a start receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void startReceiver() throws MaestroConnectionException;


    /**
     * Sends a stop receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void stopReceiver() throws MaestroConnectionException;


    /**
     * Sends a stats request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void statsRequest() throws MaestroConnectionException;


    /**
     * Sends a halt request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void halt() throws MaestroConnectionException;

    /**
     * Sends a get request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void getDataServer() throws MaestroConnectionException;

    /**
     * Sends a start agent request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void startAgent() throws MaestroConnectionException;

    /**
     * Sends a stop agent request
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void stopAgent() throws MaestroConnectionException;

    /**
     * Sends a agent general execution request
     * @param option An optional numeric option that can be associated w/ the command
     * @param payload An option string payload to be sent along w/ the command
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void userCommand(long option, final String payload) throws MaestroConnectionException;
}
