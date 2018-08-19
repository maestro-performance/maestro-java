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

import org.maestro.common.exceptions.MaestroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents the options set on the worker by the front-end
 */
public class WorkerOptions {
    private static final Logger logger = LoggerFactory.getLogger(WorkerOptions.class);

    private String brokerURL;
    private String duration;
    private String parallelCount;
    private String messageSize;
    private String rate;
    private String fcl;

    public WorkerOptions() {
    }

    public WorkerOptions(final WorkerOptions workerOptions) {
        this(workerOptions.getBrokerURL(), workerOptions.getDuration(), workerOptions.getParallelCount(),
                workerOptions.getMessageSize(), workerOptions.getRate(), workerOptions.getFcl());
    }

    public WorkerOptions(final String brokerURL, final String duration, final String parallelCount,
                         final String messageSize, final String rate, final String fcl) {
        this.brokerURL = brokerURL;
        this.duration = duration;
        this.parallelCount = parallelCount;
        this.messageSize = messageSize;
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
     * Gets the parallel count
     * @return the parallel count
     */
    public String getParallelCount() {
        return parallelCount;
    }

    public int getParallelCountAsInt() {
        try {
            return Integer.parseInt(getParallelCount());
        }
        catch (NumberFormatException e) {
            logger.warn("Unable to parse the provided parallel count {}", getParallelCount());
            throw new MaestroException("Unable to parse the provided parallel count %s",
                    getParallelCount());
        }
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
     * Gets the FCL value as a long
     * @return the FCL value as a long (or null if unset or invalid)
     */
    public long getFclAsLong() {
        if (fcl != null && !fcl.equals("0")) {
            try {
                return Long.parseLong(fcl);
            }
            catch (Exception e) {
                logger.warn("Unable to parse the provided FCL {}", fcl);
                throw new MaestroException("Unable to parse the provided FCL: %s", fcl);
            }
        }

        return -1;
    }


    /**
     * Sets the fail-condition-on-latency (FCL) fail condition
     * @param fcl The FCL value
     */
    public void setFcl(final String fcl) {
        this.fcl = fcl;
    }
}
