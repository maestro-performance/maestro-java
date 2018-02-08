package net.orpiske.mpt.common.client;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.common.exceptions.MaestroException;

import java.io.IOException;

public interface MaestroRequester {

    /**
     * Stops maestro
     * @throws MaestroConnectionException if unable to send the MQTT request
     */
    void stop() throws MaestroConnectionException;

    /**
     * Sends a flush request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void flushRequest() throws MaestroConnectionException;


    /**
     * Sends a flush request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void flushRequest(final String topic) throws MaestroConnectionException;

    /**
     * Sends a ping request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void pingRequest() throws MaestroConnectionException;

    /**
     * Sends a ping request
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void pingRequest(final String topic) throws MaestroConnectionException;


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setBroker(final String value) throws MaestroConnectionException;


    /**
     * Sends a set broker request
     * @param value The value to set the (remote) parameter to
     * @param topic the topic to send the request to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setBroker(final String topic, final String value) throws MaestroConnectionException;


    /**
     * Sends a set duration request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setDuration(final Object value) throws MaestroException;


    /**
     * Sends a set duration request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setDuration(final String topic, final Object value) throws MaestroException;



    /**
     * Sends a set log level request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setLogLevel(final String value) throws MaestroConnectionException;


    /**
     * Sends a set log level request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setLogLevel(final String topic, final String value) throws MaestroConnectionException;


    /**
     * Sends a set parallel count request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setParallelCount(final int value) throws MaestroConnectionException;


    /**
     * Sends a set parallel count request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setParallelCount(final String topic, final int value) throws MaestroConnectionException;

    /**
     *
     * Sends a set message size request (This one can be used for variable and fixed message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setMessageSize(final String value) throws MaestroConnectionException;


    /**
     * Sends a set message size request (this one can be used for fixed message sizes)
     *
     * @param value the value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setMessageSize(final long value) throws MaestroConnectionException;


    /**
     * Sends a set throttle request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setThrottle(final int value) throws MaestroConnectionException;


    /**
     * Sends a set throttle request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setThrottle(final String topic, final int value) throws MaestroConnectionException;


    /**
     * Sends a set rate request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setRate(final int value) throws MaestroConnectionException;


    /**
     * Sends a set rate request
     * @param topic the topic to send the request to
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setRate(final String topic, final int value) throws MaestroConnectionException;


    /**
     * Sends a set fail-condition-latency (FCL) request
     * @param value The value to set the (remote) parameter to
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void setFCL(final int value) throws MaestroConnectionException;


    /**
     * Sends a start inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void startInspector() throws MaestroConnectionException;


    /**
     * Sends a stop inspector request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void stopInspector() throws MaestroConnectionException;


    /**
     * Sends a start sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void startSender() throws MaestroConnectionException;


    /**
     * Sends a stop sender request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void stopSender() throws MaestroConnectionException;


    /**
     * Sends a start receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void startReceiver() throws MaestroConnectionException;


    /**
     * Sends a stop receiver request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void stopReceiver() throws MaestroConnectionException;


    /**
     * Sends a stats request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void statsRequest() throws MaestroConnectionException;


    /**
     * Sends a halt request
     * @throws MaestroConnectionException if unable to send the MQTT request
     * @throws IOException I/O and serialization errors
     */
    void halt() throws MaestroConnectionException;
}
