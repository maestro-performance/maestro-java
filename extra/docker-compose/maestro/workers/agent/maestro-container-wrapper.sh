#!/bin/bash
echo "Synchronizing time"
ntpd -d -q -n -p pool.ntp.org

${MAESTRO_APP_ROOT}/maestro-agent/bin/maestro-agent -m mqtt://broker:1883 -l /maestro/agent/logs