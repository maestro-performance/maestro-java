#!/bin/bash

if [[ ${MAESTRO_TYME_SYNC} == "true" ]] ; then
    echo "Synchronizing time"
    ntpd -d -q -n -p pool.ntp.org
fi

MAESTRO_BROKER=${MAESTRO_BROKER:-mqtt://broker:1883}
MAESTRO_LOG_DIR=${MAESTRO_LOG_DIR:-/maestro/agent/logs}

${MAESTRO_APP_ROOT}/maestro-agent/bin/maestro-agent -m ${MAESTRO_BROKER} -l ${MAESTRO_LOG_DIR}
