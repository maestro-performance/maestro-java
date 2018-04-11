#!/bin/bash
echo "Synchronizing time"
ntpd -d -q -n -p pool.ntp.org

cd /maestro/receiver/logs && nohup python -m SimpleHTTPServer 8000 &

${MAESTRO_APP_ROOT}/maestro-worker/bin/maestro-worker -m mqtt://maestro-broker:1883 -w org.maestro.worker.jms.JMSReceiverWorker -l /maestro/receiver/logs -r receiver -H maestro-receiver