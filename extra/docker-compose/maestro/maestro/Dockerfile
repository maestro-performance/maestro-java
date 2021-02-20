FROM fedora:33 AS buildimg
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
ARG MAESTRO_VERSION
ENV MAESTRO_VERSION ${MAESTRO_VERSION:-1.6.0-SNAPSHOT}
ARG MAESTRO_BRANCH
ENV MAESTRO_BRANCH ${MAESTRO_BRANCH:-devel}
LABEL MAESTRO_VERSION=${MAESTRO_VERSION}
RUN dnf install -y java-11-openjdk-headless which unzip zip wget && dnf clean all
ENV JAVA_HOME /etc/alternatives/jre
WORKDIR /root/build
RUN wget https://github.com/maestro-performance/maestro-java/archive/${MAESTRO_BRANCH}.zip -O maestro-java.zip && \
    unzip maestro-java.zip && \
    ln -s maestro-java-${MAESTRO_BRANCH} maestro-java && \
    cd maestro-java && \
    ./mvnw -DskipTests=true -PPackage clean package

ENV MAESTRO_APP_ROOT /opt/maestro
RUN mkdir -p ${MAESTRO_APP_ROOT}
WORKDIR ${MAESTRO_APP_ROOT}
ENV MAESTRO_WORKER_TYPE worker
RUN mkdir -p maestro-${MAESTRO_WORKER_TYPE} && tar --strip-components=1 -xvf /root/build/maestro-java/maestro-${MAESTRO_WORKER_TYPE}/target/maestro-${MAESTRO_WORKER_TYPE}-${MAESTRO_VERSION}-bin.tar.gz -C maestro-${MAESTRO_WORKER_TYPE}
ENV MAESTRO_WORKER_TYPE agent
RUN mkdir -p maestro-${MAESTRO_WORKER_TYPE} && tar --strip-components=1 -xvf /root/build/maestro-java/maestro-${MAESTRO_WORKER_TYPE}/target/maestro-${MAESTRO_WORKER_TYPE}-${MAESTRO_VERSION}-bin.tar.gz -C maestro-${MAESTRO_WORKER_TYPE}
ENV MAESTRO_WORKER_TYPE inspector
RUN mkdir -p maestro-${MAESTRO_WORKER_TYPE} && tar --strip-components=1 -xvf /root/build/maestro-java/maestro-${MAESTRO_WORKER_TYPE}/target/maestro-${MAESTRO_WORKER_TYPE}-${MAESTRO_VERSION}-bin.tar.gz -C maestro-${MAESTRO_WORKER_TYPE}
ENV MAESTRO_WORKER_TYPE exporter
RUN mkdir -p maestro-${MAESTRO_WORKER_TYPE} && tar --strip-components=1 -xvf /root/build/maestro-java/maestro-${MAESTRO_WORKER_TYPE}/target/maestro-${MAESTRO_WORKER_TYPE}-${MAESTRO_VERSION}-bin.tar.gz -C maestro-${MAESTRO_WORKER_TYPE}
ENV MAESTRO_WORKER_TYPE reports
RUN mkdir -p maestro-${MAESTRO_WORKER_TYPE}-tool && tar --strip-components=1 -xvf /root/build/maestro-java/maestro-${MAESTRO_WORKER_TYPE}/maestro-${MAESTRO_WORKER_TYPE}-tool/target/maestro-${MAESTRO_WORKER_TYPE}-tool-${MAESTRO_VERSION}-bin.tar.gz -C maestro-${MAESTRO_WORKER_TYPE}-tool
RUN mkdir -p maestro-cli && tar --strip-components=1 -xvf /root/build/maestro-java/maestro-cli/target/maestro-cli-${MAESTRO_VERSION}-bin.tar.gz -C maestro-cli


# Base image for everything
FROM registry.access.redhat.com/ubi8/ubi-minimal AS maestro-base
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
ARG MAESTRO_VERSION
ENV MAESTRO_VERSION ${MAESTRO_VERSION:-1.6.0-SNAPSHOT}
LABEL MAESTRO_VERSION=${MAESTRO_VERSION}
ENV MAESTRO_APP_ROOT /opt/maestro
ENV JAVA_HOME /etc/alternatives/jre
RUN mkdir -p ${MAESTRO_APP_ROOT} && \
    mkdir -p /maestro && \
    microdnf install -y java-11-openjdk-headless unzip which rsync openssl && \
    microdnf clean all
WORKDIR ${MAESTRO_APP_ROOT}

## Worker ##
FROM maestro-base AS maestro-worker
ENV MAESTRO_WORKER_TYPE worker
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
LABEL MAESTRO_WORKER_TYPE=${MAESTRO_WORKER_TYPE}
COPY --from=buildimg ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE} maestro-${MAESTRO_WORKER_TYPE}

ADD ${MAESTRO_WORKER_TYPE}/log4j.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}/config/log4j.properties
ADD ${MAESTRO_WORKER_TYPE}/maestro-${MAESTRO_WORKER_TYPE}.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}/config/maestro-${MAESTRO_WORKER_TYPE}.properties
RUN mkdir -p /maestro/${MAESTRO_WORKER_TYPE}/logs
ADD ${MAESTRO_WORKER_TYPE}/maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]


## Inspector ##
FROM maestro-base AS maestro-inspector
ENV MAESTRO_WORKER_TYPE inspector
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
LABEL MAESTRO_WORKER_TYPE=${MAESTRO_WORKER_TYPE}
COPY --from=buildimg ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE} maestro-${MAESTRO_WORKER_TYPE}

ADD ${MAESTRO_WORKER_TYPE}/log4j.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}/config/log4j.properties
ADD ${MAESTRO_WORKER_TYPE}/maestro-${MAESTRO_WORKER_TYPE}.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}/config/maestro-${MAESTRO_WORKER_TYPE}.properties
RUN mkdir -p /maestro/${MAESTRO_WORKER_TYPE}/logs
ADD ${MAESTRO_WORKER_TYPE}/maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]

## Exporter ##
FROM maestro-base AS maestro-exporter
ENV MAESTRO_WORKER_TYPE exporter
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
LABEL MAESTRO_WORKER_TYPE=${MAESTRO_WORKER_TYPE}
EXPOSE 9120
COPY --from=buildimg ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE} maestro-${MAESTRO_WORKER_TYPE}

ADD ${MAESTRO_WORKER_TYPE}/log4j.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}/config/log4j.properties
ADD ${MAESTRO_WORKER_TYPE}/maestro-exporter.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}/config/maestro-${MAESTRO_WORKER_TYPE}.properties
RUN mkdir -p /maestro/${MAESTRO_WORKER_TYPE}/logs
ADD ${MAESTRO_WORKER_TYPE}/maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]


## Agent ##
FROM maestro-base AS maestro-agent
ENV MAESTRO_WORKER_TYPE agent
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
LABEL MAESTRO_WORKER_TYPE=${MAESTRO_WORKER_TYPE}
COPY --from=buildimg ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE} maestro-${MAESTRO_WORKER_TYPE}

ADD ${MAESTRO_WORKER_TYPE}/log4j.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}/config/log4j.properties
ADD ${MAESTRO_WORKER_TYPE}/maestro-${MAESTRO_WORKER_TYPE}.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}/config/maestro-${MAESTRO_WORKER_TYPE}.properties
RUN mkdir -p /maestro/${MAESTRO_WORKER_TYPE}/logs
ADD ${MAESTRO_WORKER_TYPE}/maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]


## Reports Collector/Server ##
FROM maestro-base AS maestro-reports
ENV MAESTRO_WORKER_TYPE reports
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
LABEL MAESTRO_WORKER_TYPE=${MAESTRO_WORKER_TYPE}
EXPOSE 6500
COPY --from=buildimg ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}-tool maestro-${MAESTRO_WORKER_TYPE}-tool

ADD ${MAESTRO_WORKER_TYPE}/log4j.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}-tool/config/log4j.properties
ADD ${MAESTRO_WORKER_TYPE}/maestro-${MAESTRO_WORKER_TYPE}-tool.properties ${MAESTRO_APP_ROOT}/maestro-${MAESTRO_WORKER_TYPE}-tool/config/maestro-${MAESTRO_WORKER_TYPE}-tool.properties
RUN mkdir -p /maestro/${MAESTRO_WORKER_TYPE}-tool
VOLUME /maestro
ADD ${MAESTRO_WORKER_TYPE}/maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]

## Client ##
FROM maestro-base as maestro-client
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
LABEL MAESTRO_WORKER_TYPE=client
ENV GROOVY_VERSION 2.4.15
LABEL MAESTRO_WORKER_TYPE=${MAESTRO_WORKER_TYPE}
RUN microdnf install -y java-11-openjdk && \
    microdnf clean all
COPY --from=buildimg ${MAESTRO_APP_ROOT}/maestro-cli maestro-cli

ENV MAESTRO_BROKER mqtt://broker:1883
ENV SEND_RECEIVE_URL amqp://sut:5672/test.performance.queue
ENV MESSAGE_SIZE 256
ENV TEST_DURATION 90s
ENV RATE 0
ENV PARALLEL_COUNT 2

ADD client/motd.txt /etc/motd
RUN echo "export PATH=\$PATH:/opt/maestro/maestro-cli/bin/" >> $HOME/.bashrc
RUN echo "cat /etc/motd" >> $HOME/.bashrc

ADD client/maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
ADD client/setup-artemis-inspector.sh /usr/bin/setup-artemis-inspector
ADD client/setup-interconnect-inspector.sh /usr/bin/setup-interconnect-inspector
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]


