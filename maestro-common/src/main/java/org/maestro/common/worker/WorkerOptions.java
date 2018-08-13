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

package org.maestro.common.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents the options set on the worker by the front-end
 */
public class WorkerOptions {
    private static final Logger logger = LoggerFactory.getLogger(WorkerOptions.class);

    private String brokerURL;
    private String duration;
    private String logLevel;
    private String parallelCount;
    private String messageSize;
    private String throttle;
    private String rate;
    private String fcl;

    public WorkerOptions() {
    }

    public WorkerOptions(final WorkerOptions workerOptions) {
        this(workerOptions.getBrokerURL(), workerOptions.getDuration(), workerOptions.getLogLevel(),
                workerOptions.getParallelCount(), workerOptions.getMessageSize(), workerOptions.getThrottle(),
                workerOptions.getRate(), workerOptions.getFcl());
    }

    public WorkerOptions(final String brokerURL, final String duration, final String logLevel,
                         final String parallelCount, final String messageSize, final String throttle,
                         final String rate, final String fcl) {
        this.brokerURL = brokerURL;
        this.duration = duration;
        this.logLevel = logLevel;
        this.parallelCount = parallelCount;
        this.messageSize = messageSize;
        this.throttle = throttle;
        this.rate = rate;
        this.fcl = fcl;
    }

    /**
     * Gets the broker URL
     * @return the broker URL
     */
    public String getBrokerURL() {
        return brokerURL;
    }

    /**
     * Set the broker url
     * @param brokerURL the broker url
     */
    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }


    /**
     * The test duration as a string
     * @return the test duration as a string
     */
    public String getDuration() {
        return duration;
    }


    /**
     * Sets the test duration
     * @param duration the test duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }


    /**
     * Gets the log level
     * @return the log level
     */
    public String getLogLevel() {
        return logLevel;
    }


    /**
     * Set the log level
     * @param logLevel the log level
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Gets the parallel count
     * @return the parallel count
     */
    public String getParallelCount() {
        return parallelCount;
    }

    public Integer getParallelCountAsInt() {
        try {
            return Integer.parseInt(getParallelCount());
        }
        catch (NumberFormatException e) {
            logger.warn("Unable to parse the provided parallel count {}", getParallelCount());
        }

        return null;
    }

    /**
     * Sets the number of concurrent connections (aka parallel count)
     * @param parallelCount the parallel count
     */
    public void setParallelCount(String parallelCount) {
        this.parallelCount = parallelCount;
    }


    /**
     * Gets the message size
     * @return the message size (either in the numeric form or with the variable size format)
     */
    public String getMessageSize() {
        return messageSize;
    }


    /**
     * Sets the message size
     * @param messageSize the message size (either in the numeric form or with the variable size format)
     */
    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }


    /**
     * Gets the throttling
     * @return the throttling value
     */
    public String getThrottle() {
        return throttle;
    }

    /**
     * Sets the throttling value
     * @param throttle throttle value
     */
    public void setThrottle(String throttle) {
        this.throttle = throttle;
    }


    /**
     * Gets the target rate
     * @return the target rate
     */
    public String getRate() {
        return rate;
    }


    /**
     * Sets the target rate
     * @param rate the target rate
     */
    public void setRate(String rate) {
        this.rate = rate;
    }


    /**
     * Gets the FCL value
     * @return the FCL value
     */
    public String getFcl() {
        return fcl;
    }


    /**
     * Gets the FCL value as a double
     * @return the FCL value as a double (or null if unset or invalid)
     */
    public Double getFclAsDouble() {
        if (fcl != null && !fcl.equals("0")) {
            try {
                return Double.parseDouble(fcl);
            }
            catch (Exception e) {
                logger.warn("Unable to parse the provided FCL {}", fcl);
            }
        }

        return null;
    }


    /**
     * Sets the fail-condition-on-latency (FCL) fail condition
     * @param fcl The FCL value
     */
    public void setFcl(final String fcl) {
        this.fcl = fcl;
    }
}
