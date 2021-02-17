FROM registry.access.redhat.com/ubi8/ubi-minimal as artemis-base-broker
LABEL maintainer="angusyoung@gmail.com"
ARG ARTEMIS_VERSION
ENV ARTEMIS_VERSION ${ARTEMIS_VERSION:-2.17.0}
ARG ARTEMIS_JOURNAL
ENV ARTEMIS_JOURNAL ${ARTEMIS_JOURNAL:-aio}
ENV MAESTRO_SUT_ROOT /opt/maestro/sut/
ENV MAESTRO_DATA_ROOT /maestro
VOLUME /maestro
EXPOSE 1883 5672 61616
RUN microdnf install -y java-11-openjdk-headless libaio tar gzip && \
    mkdir -p ${MAESTRO_SUT_ROOT} && \
    cd ${MAESTRO_SUT_ROOT} && \
    curl https://archive.apache.org/dist/activemq/activemq-artemis/${ARTEMIS_VERSION}/apache-artemis-${ARTEMIS_VERSION}-bin.tar.gz -o apache-artemis.tar.gz && \
    mkdir -p apache-artemis && tar --strip-components=1 --exclude=examples/* -xvf apache-artemis.tar.gz -C apache-artemis && \
    rm -f apache-artemis.tar.gz && \
    microdnf remove tar gzip && \
    microdnf clean all
WORKDIR ${MAESTRO_SUT_ROOT}
ENV JAVA_HOME /etc/alternatives/jre

FROM artemis-base-broker
RUN ${MAESTRO_SUT_ROOT}/apache-artemis/bin/artemis create --${ARTEMIS_JOURNAL} --allow-anonymous --relax-jolokia --http-host 0.0.0.0 --user admin --password "admin" --role amq --data ${MAESTRO_DATA_ROOT} ${MAESTRO_SUT_ROOT}/apache-artemis-instance
CMD [ "sh", "-c", "./apache-artemis-instance/bin/artemis run" ]
