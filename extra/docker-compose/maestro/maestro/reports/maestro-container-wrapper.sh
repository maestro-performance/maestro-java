#!/bin/bash

MAESTRO_BROKER=${MAESTRO_BROKER:-mqtt://broker:1883}
MAESTRO_DATA_DIR=${MAESTRO_DATA_DIR:-/maestro/reports-tool/data}

${MAESTRO_APP_ROOT}/maestro-reports-tool/bin/maestro-reports-tool -m ${MAESTRO_BROKER} -d ${MAESTRO_DATA_DIR}
