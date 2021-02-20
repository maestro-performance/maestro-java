#!/bin/bash

MAESTRO_BROKER=${MAESTRO_BROKER:-mqtt://broker:1883}
MAESTRO_LOG_DIR=${MAESTRO_LOG_DIR:-/maestro/worker/logs}

${MAESTRO_APP_ROOT}/maestro-worker/bin/maestro-worker -m ${MAESTRO_BROKER} -l ${MAESTRO_LOG_DIR}
