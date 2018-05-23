#!/bin/bash

echo "Enabling Artemis inspector"
export INSPECTOR_NAME=ArtemisInspector
export MANAGEMENT_INTERFACE=http://admin:admin@sut:8161/console/jolokia