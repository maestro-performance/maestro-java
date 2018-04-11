FROM centos:7
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
ARG MAESTRO_WORKER_VERSION
ENV MAESTRO_WORKER_VERSION ${MAESTRO_WORKER_VERSION:-1.2.0}
ENV MAESTRO_APP_ROOT /opt/maestro
EXPOSE 8000
RUN mkdir -p ${MAESTRO_APP_ROOT}
WORKDIR ${MAESTRO_APP_ROOT}
RUN yum install -y java-1.8.0-openjdk ntp
ENV JAVA_HOME /etc/alternatives/jre
RUN curl http://orpiske.net/files/maestro-java/${MAESTRO_WORKER_VERSION}/maestro-worker-${MAESTRO_WORKER_VERSION}-bin.tar.gz -o maestro-worker.tar.gz
RUN tar -xvf maestro-worker.tar.gz && ln -s ${MAESTRO_APP_ROOT}/maestro-worker-${MAESTRO_WORKER_VERSION} ${MAESTRO_APP_ROOT}/maestro-worker
RUN rm -f maestro-worker.tar.gz
RUN mkdir -p /maestro
VOLUME /maestro