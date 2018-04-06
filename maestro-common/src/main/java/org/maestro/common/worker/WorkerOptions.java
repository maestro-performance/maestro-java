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

/**
 * Represents the options set on the worker by the front-end
 */
public class WorkerOptions {
    private String brokerURL;
    private String duration;
    private String logLevel;
    private String parallelCount;
    private String messageSize;
    private String throttle;
    private String rate;
    private String fcl;

    /**
     * Gets the broker URL
     * @return the broker URL
     */
    public String getBrokerURL() {
        return brokerURL;
    }

    /**
     * Set the broker url
     * @param brokerURL
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
     * @param duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }


    /**
     * Gets the log level
     * @return
     */
    public String getLogLevel() {
        return logLevel;
    }


    /**
     * Set the log level
     * @param logLevel
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getParallelCount() {
        return parallelCount;
    }

    /**
     * Sets the number of concurrent connections
     * @param parallelCount
     */
    public void setParallelCount(String parallelCount) {
        this.parallelCount = parallelCount;
    }

    public String getMessageSize() {
        return messageSize;
    }


    /**
     * Sets the message size
     * @param messageSize
     */
    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }

    public String getThrottle() {
        return throttle;
    }

    /**
     * Sets the throttling value
     * @param throttle
     */
    public void setThrottle(String throttle) {
        this.throttle = throttle;
    }

    public String getRate() {
        return rate;
    }


    /**
     * Sets the target rate
     * @param rate
     */
    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getFcl() {
        return fcl;
    }


    public Double getFclAsDouble() {
        if (fcl != null) {
            try {
                return Double.parseDouble(fcl);
            }
            catch (Exception e) {

            }
        }

        return null;
    }


    /**
     * Sets the fail-condition-on-latency fail condition
     * @param fcl
     */
    public void setFcl(String fcl) {
        this.fcl = fcl;
    }
}
