package org.maestro.common.worker;

public class WorkerOptions {
    private String brokerURL;
    private String duration;
    private String logLevel;
    private String parallelCount;
    private String messageSize;
    private String throttle;
    private String rate;
    private String fcl;

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

    public String getDuration() {
        return duration;
    }



    public void setDuration(String duration) {
        this.duration = duration;
    }

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
