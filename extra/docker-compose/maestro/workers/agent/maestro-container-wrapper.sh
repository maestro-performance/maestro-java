#!/bin/bash
echo "Synchronizing time"
ntpd -d -q -n -p pool.ntp.org
MAESTRO_BROKER=${MAESTRO_BROKER:-mqtt://broker:1883}
MAESTRO_LOG_DIR=${MAESTRO_LOG_DIR:-/maestro/receiver/logs}

${MAESTRO_APP_ROOT}/maestro-agent/bin/maestro-agent -m ${MAESTRO_BROKER} -l ${MAESTRO_LOG_DIR}
