#!/bin/bash

MAESTRO_TYME_SYNC=${MAESTRO_TYME_SYNC:-true}
if [[ ${MAESTRO_TYME_SYNC} == "true" ]] ; then
    echo "Synchronizing time"
    ntpd -d -q -n -p pool.ntp.org
fi


MAESTRO_BROKER=${MAESTRO_BROKER:-mqtt://broker:1883}
MAESTRO_LOG_DIR=${MAESTRO_LOG_DIR:-/maestro/receiver/logs}

${MAESTRO_APP_ROOT}/maestro-worker/bin/maestro-worker -m ${MAESTRO_BROKER} -w org.maestro.worker.jms.JMSSenderWorker -l ${MAESTRO_LOG_DIR} -r sender
