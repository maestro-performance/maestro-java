#!/bin/bash
echo "Synchronizing time"
ntpd -d -q -n -p pool.ntp.org

${MAESTRO_APP_ROOT}/maestro-inspector/bin/maestro-inspector -m mqtt://broker:1883 -l /maestro/inspector/logs -H inspector