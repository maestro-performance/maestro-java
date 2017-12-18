Maestro Export
============

Introduction:
----

A exporter module for [Prometheus](https://prometheus.io/). Runs on port 9200 by default.


Sample Configuration
----

Add this to your `prometheus.yml` file:


```  - job_name: 'maestro'
       scrape_interval: "15s"
       static_configs:
         - targets: ["maestro.exporter.host.com:9200"]
```


Sample Data
----

`````# HELP maestro_rate Rate
# TYPE maestro_rate gauge
maestro_rate{peer="sender@perf-dev-node-01.test.host.com",type="sender",} 4002.0
# HELP maestro_abnormal_disconnect Abnormal disconnect count
# TYPE maestro_abnormal_disconnect counter
maestro_abnormal_disconnect 0.0
# HELP maestro_connection_count Connection count
# TYPE maestro_connection_count gauge
maestro_connection_count{peer="receiver@perf-dev-node-02.test.host.com",type="receiver",} 4.0
# HELP maestro_message_count Connection count
# TYPE maestro_message_count gauge
maestro_message_count{peer="receiver@perf-dev-node-02.test.host.com",type="received",} 3.8613075E7
# HELP maestro_ping Ping
# TYPE maestro_ping gauge
maestro_ping{peer="receiver@perf-dev-node-02.test.host.com",type="receiver",} 60.0
# HELP maestro_rate Rate
# TYPE maestro_rate gauge
maestro_rate{peer="receiver@perf-dev-node-02.test.host.com",type="receiver",} 4077.0
# HELP maestro_failures Test failures
# TYPE maestro_failures counter
maestro_failures 0.0
# HELP maestro_message_count Connection count
# TYPE maestro_message_count gauge
maestro_message_count{peer="sender@perf-dev-node-01.test.host.com",type="sent",} 3.9311315E7
# HELP maestro_ping Ping
# TYPE maestro_ping gauge
maestro_ping{peer="sender@perf-dev-node-01.test.host.com",type="sender",} 60.0
# HELP maestro_success Test success
# TYPE maestro_success counter
maestro_success 1.0
# HELP maestro_connection_count Connection count
# TYPE maestro_connection_count gauge
maestro_connection_count{peer="sender@perf-dev-node-01.test.host.com",type="sender",} 4.0
```