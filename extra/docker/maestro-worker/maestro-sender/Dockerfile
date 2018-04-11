FROM centos:7
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
ARG MAESTRO_WORKER_VERSION
ENV MAESTRO_WORKER_VERSION ${MAESTRO_WORKER_VERSION:-1.2.0}
LABEL MAESTRO_VERSION=${MAESTRO_WORKER_VERSION}
ENV MAESTRO_APP_ROOT /opt/maestro
EXPOSE 8000
RUN mkdir -p ${MAESTRO_APP_ROOT}
WORKDIR ${MAESTRO_APP_ROOT}
RUN yum install -y java-1.8.0-openjdk ntp
ENV JAVA_HOME /etc/alternatives/jre
RUN curl http://orpiske.net/files/maestro-java/${MAESTRO_WORKER_VERSION}/maestro-worker-${MAESTRO_WORKER_VERSION}-bin.tar.gz -o maestro-worker.tar.gz
RUN mkdir maestro-worker && tar --strip-components=1 -xvf maestro-worker.tar.gz -C maestro-worker
RUN rm -f maestro-worker.tar.gz
RUN mkdir -p /maestro
VOLUME /maestro

ADD log4j.properties ${MAESTRO_APP_ROOT}/maestro-worker/config/log4j.properties
RUN mkdir -p /maestro/
VOLUME /maestro/
RUN mkdir -p /maestro/sender/logs
ADD maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]
