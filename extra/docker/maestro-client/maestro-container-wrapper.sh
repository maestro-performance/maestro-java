#!/bin/bash

echo "Synchronizing time"
ntpd -d -q -n -p pool.ntp.org
mkdir -p /maestro/reports
cd /maestro/reports && nohup python -m SimpleHTTPServer 8000 &
cd /opt/maestro/maestro-test-scripts
/bin/bash