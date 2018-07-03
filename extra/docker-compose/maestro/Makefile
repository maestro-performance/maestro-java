MAESTRO_VERSION=1.3.0
MAESTRO_BRANCH=maestro-java-1.3.0

.PHONY: broker clean all client

broker:
	cd broker && docker build -t maestroperf/maestro-broker:${MAESTRO_VERSION} --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} .

client:
	cd client && docker build -t maestroperf/maestro-client:${MAESTRO_VERSION} --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} .

worker-sender:
	cd workers/sender && docker build -t maestroperf/maestro-sender:${MAESTRO_VERSION} --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} .

worker-receiver:
	cd workers/receiver && docker build -t maestroperf/maestro-receiver:${MAESTRO_VERSION} --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} .

agent:
	cd workers/agent && docker build -t maestroperf/maestro-agent:${MAESTRO_VERSION} --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} .

inspector:
	cd workers/inspector && docker build -t maestroperf/maestro-inspector:${MAESTRO_VERSION} --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} .

build: broker client worker-sender worker-receiver agent inspector

push: build
	docker push maestroperf/maestro-broker:${MAESTRO_VERSION}
	docker push maestroperf/maestro-client:${MAESTRO_VERSION}
	docker push maestroperf/maestro-receiver:${MAESTRO_VERSION}
	docker push maestroperf/maestro-sender:${MAESTRO_VERSION}
	docker push maestroperf/maestro-agent:${MAESTRO_VERSION}
	docker push maestroperf/maestro-inspector:${MAESTRO_VERSION}


all: push
