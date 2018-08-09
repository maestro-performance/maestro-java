#!/bin/bash

if [[ ${MAESTRO_TYME_SYNC} == "true" ]] ; then
    echo "Synchronizing time"
    ntpd -d -q -n -p pool.ntp.org
fi

mkdir -p /maestro/reports
cd /maestro/reports
cd /opt/maestro/maestro-test-scripts
/bin/bash