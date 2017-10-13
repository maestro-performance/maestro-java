package net.orpiske.mpt.test;

import java.util.List;

public interface MultiPointProfile {
    final class EndPoint {
        private String topic;
        private String name;
        private String brokerURL;


        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBrokerURL() {
            return brokerURL;
        }

        public void setBrokerURL(String brokerURL) {
            this.brokerURL = brokerURL;
        }
    }

    void addEndPoint(EndPoint endPoint);
    List<EndPoint> getEndPoints();
}
