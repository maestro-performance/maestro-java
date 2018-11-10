MAESTRO_VERSION?=1.5.0-SNAPSHOT
MAESTRO_BRANCH?=devel
MAESTRO_LABEL_VERSION?=${MAESTRO_VERSION}

clean:
	docker rmi -f maestroperf/maestro-broker:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-client:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-agent:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-inspector:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-exporter:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-worker:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-reports:${MAESTRO_VERSION}
	docker rmi -f maestro_client maestro_agent maestro_reports maestro_inspector maestro_worker maestro_broker maestro_exporter

broker:
	cd broker && docker build -t maestroperf/maestro-broker:${MAESTRO_VERSION} --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} .

maestro:
	cd maestro && docker build --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} -t maestroperf/maestro-worker:${MAESTRO_VERSION} -t maestroperf/maestro-worker:${MAESTRO_LABEL_VERSION}  --target maestro-worker .
	cd maestro && docker build --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} -t maestroperf/maestro-inspector:${MAESTRO_VERSION} -t maestroperf/maestro-inspector:${MAESTRO_LABEL_VERSION} --target maestro-inspector .
	cd maestro && docker build --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} -t maestroperf/maestro-agent:${MAESTRO_VERSION} -t maestroperf/maestro-agent:${MAESTRO_LABEL_VERSION} --target maestro-agent .
	cd maestro && docker build --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} -t maestroperf/maestro-exporter:${MAESTRO_VERSION} -t maestroperf/maestro-exporter:${MAESTRO_LABEL_VERSION} --target maestro-exporter .
	cd maestro && docker build --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} -t maestroperf/maestro-client:${MAESTRO_VERSION} -t maestroperf/maestro-client:${MAESTRO_LABEL_VERSION} --target maestro-client .
	cd maestro && docker build --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} -t maestroperf/maestro-reports:${MAESTRO_VERSION} -t maestroperf/maestro-reports:${MAESTRO_LABEL_VERSION} --target maestro-reports .

build: broker maestro

push: build
	docker push maestroperf/maestro-broker:${MAESTRO_VERSION}
	docker push maestroperf/maestro-client:${MAESTRO_VERSION}
	docker push maestroperf/maestro-worker:${MAESTRO_VERSION}
	docker push maestroperf/maestro-agent:${MAESTRO_VERSION}
	docker push maestroperf/maestro-inspector:${MAESTRO_VERSION}
	docker push maestroperf/maestro-exporter:${MAESTRO_VERSION}
	docker push maestroperf/maestro-reports:${MAESTRO_VERSION}

all: push

.PHONY: broker clean all client exporter maestro