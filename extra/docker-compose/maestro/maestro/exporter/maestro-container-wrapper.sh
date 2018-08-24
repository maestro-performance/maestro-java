#!/bin/bash
echo "Synchronizing time"
ntpd -d -q -n -p pool.ntp.org
MAESTRO_BROKER=${MAESTRO_BROKER:-mqtt://broker:1883}
MAESTRO_LOG_DIR=${MAESTRO_LOG_DIR:-/maestro/exporter/logs}
MAESTRO_EXPORTER_PORT=${MAESTRO_EXPORTER_PORT:-9120}

${MAESTRO_APP_ROOT}/maestro-exporter/bin/maestro-exporter -m ${MAESTRO_BROKER} -p ${MAESTRO_EXPORTER_PORT}
