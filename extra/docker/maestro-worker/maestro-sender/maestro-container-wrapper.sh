#!/bin/bash
echo "Synchronizing time"
ntpd -d -q -n -p pool.ntp.org

cd /maestro/sender/logs && nohup python -m SimpleHTTPServer 8000 &

${MAESTRO_APP_ROOT}/maestro-worker/bin/maestro-worker -m mqtt://maestro-broker:1883 -w org.maestro.worker.jms.JMSSenderWorker -l /maestro/sender/logs -r sender -H maestro-sender