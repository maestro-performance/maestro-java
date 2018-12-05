MAESTRO_VERSION?=1.5.0
MAESTRO_BRANCH?=devel
MAESTRO_LABEL_VERSION?=${MAESTRO_BRANCH}
ACTIVEMQ_VERSION?=5.15.2
ARTEMIS_VERSION?=2.6.3
QPID_DISPATCH_VERSION?=1.4.0
QPID_PROTON_VERSION?=0.24.0

default: build

clean:
	docker rmi -f maestroperf/maestro-broker:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-broker:${MAESTRO_LABEL_VERSION}
	docker rmi -f maestroperf/maestro-client:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-client:${MAESTRO_LABEL_VERSION}
	docker rmi -f maestroperf/maestro-agent:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-agent:${MAESTRO_LABEL_VERSION}
	docker rmi -f maestroperf/maestro-inspector:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-inspector:${MAESTRO_LABEL_VERSION}
	docker rmi -f maestroperf/maestro-exporter:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-exporter:${MAESTRO_LABEL_VERSION}
	docker rmi -f maestroperf/maestro-worker:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-worker:${MAESTRO_LABEL_VERSION}
	docker rmi -f maestroperf/maestro-reports:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-reports:${MAESTRO_LABEL_VERSION}
	docker rmi -f maestro_client maestro_agent maestro_reports maestro_inspector maestro_worker maestro_broker maestro_exporter


suts:
	cd suts/apache-activemq && docker build --build-arg ACTIVEMQ_VERSION=${ACTIVEMQ_VERSION} -t maestroci/activemq:${ACTIVEMQ_VERSION} -t maestroci/activemq:${MAESTRO_LABEL_VERSION} .
	cd suts/apache-artemis && docker build --build-arg ARTEMIS_VERSION=${ARTEMIS_VERSION} -t maestroci/artemis:${ARTEMIS_VERSION} -t maestroci/artemis:${MAESTRO_LABEL_VERSION} --target artemis .
	cd suts/apache-artemis && docker build --build-arg ARTEMIS_VERSION=${ARTEMIS_VERSION} -t maestroci/artemis-tls:${ARTEMIS_VERSION} -t maestroci/artemis-tls:${MAESTRO_LABEL_VERSION} --target artemis-tls .
	cd suts/qpid-dispatch && docker build --build-arg QPID_DISPATCH_VERSION=${QPID_DISPATCH_VERSION} --build-arg QPID_PROTON_VERSION=${QPID_PROTON_VERSION} -t maestroci/qpid-dispatch:${QPID_DISPATCH_VERSION} -t maestroci/qpid-dispatch:${MAESTRO_LABEL_VERSION} .

push-suts: suts
	docker push maestroci/activemq:${ACTIVEMQ_VERSION}
	docker push maestroci/activemq:${MAESTRO_LABEL_VERSION}
	docker push maestroci/artemis:${ARTEMIS_VERSION}
	docker push maestroci/artemis:${MAESTRO_LABEL_VERSION}
	docker push maestroci/artemis-tls:${ARTEMIS_VERSION}
	docker push maestroci/artemis-tls:${MAESTRO_LABEL_VERSION}
	docker push maestroci/qpid-dispatch:${QPID_DISPATCH_VERSION}
	docker push maestroci/qpid-dispatch:${MAESTRO_LABEL_VERSION}

broker:
	cd broker && docker build -t maestroperf/maestro-broker:${MAESTRO_VERSION} -t maestroperf/maestro-broker:${MAESTRO_LABEL_VERSION} --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} .

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
	docker push maestroperf/maestro-broker:${MAESTRO_LABEL_VERSION}
	docker push maestroperf/maestro-client:${MAESTRO_VERSION}
	docker push maestroperf/maestro-client:${MAESTRO_LABEL_VERSION}
	docker push maestroperf/maestro-worker:${MAESTRO_VERSION}
	docker push maestroperf/maestro-worker:${MAESTRO_LABEL_VERSION}
	docker push maestroperf/maestro-agent:${MAESTRO_VERSION}
	docker push maestroperf/maestro-agent:${MAESTRO_LABEL_VERSION}
	docker push maestroperf/maestro-inspector:${MAESTRO_VERSION}
	docker push maestroperf/maestro-inspector:${MAESTRO_LABEL_VERSION}
	docker push maestroperf/maestro-exporter:${MAESTRO_VERSION}
	docker push maestroperf/maestro-exporter:${MAESTRO_LABEL_VERSION}
	docker push maestroperf/maestro-reports:${MAESTRO_VERSION}
	docker push maestroperf/maestro-reports:${MAESTRO_LABEL_VERSION}

all: push

.PHONY: broker clean all client exporter maestro suts