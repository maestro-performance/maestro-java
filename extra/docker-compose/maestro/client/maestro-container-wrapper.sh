#!/bin/bash

echo "Synchronizing time"
ntpd -d -q -n -p pool.ntp.org
mkdir -p /maestro/reports
cd /maestro/reports
cd /opt/maestro/maestro-test-scripts
/bin/bash