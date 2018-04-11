FROM centos:7
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
ARG MAESTRO_WORKER_VERSION
ENV MAESTRO_WORKER_VERSION ${MAESTRO_WORKER_VERSION:-1.2.0}
LABEL MAESTRO_VERSION=${MAESTRO_WORKER_VERSION}
ENV GROOVY_VERSION 2.4.14

RUN yum install -y java-1.8.0-openjdk unzip zip which ntp
ENV JAVA_HOME /etc/alternatives/jre

RUN mkdir -p /opt/groovy/
RUN curl -s get.sdkman.io | bash
RUN source "/root/.sdkman/bin/sdkman-init.sh" && sdk install groovy ${GROOVY_VERSION}

ENV MAESTRO_APP_ROOT /opt/maestro
RUN mkdir -p ${MAESTRO_APP_ROOT}
WORKDIR ${MAESTRO_APP_ROOT}

RUN curl http://orpiske.net/files/maestro-java/${MAESTRO_WORKER_VERSION}/maestro-cli-${MAESTRO_WORKER_VERSION}-bin.tar.gz -o maestro-cli.tar.gz
RUN mkdir maestro-cli && tar --strip-components=1 -xvf maestro-cli.tar.gz -C maestro-cli
RUN rm -f maestro-cli.tar.gz

RUN curl http://orpiske.net/files/maestro-java/${MAESTRO_WORKER_VERSION}/maestro-test-scripts-${MAESTRO_WORKER_VERSION}-bin.tar.gz -o maestro-test-scripts.tar.gz
RUN mkdir maestro-test-scripts && tar --strip-components=1 -xvf maestro-test-scripts.tar.gz -C maestro-test-scripts
RUN rm -f maestro-test-scripts.tar.gz

ENV MAESTRO_BROKER mqtt://maestro-broker:1883
ENV BROKER_URL amqp://maestro-sut:5672/test.performance.queue
ENV MESSAGE_SIZE 256
ENV TEST_DURATION 90s
ENV RATE 0
ENV PARALLEL_COUNT 2

ADD motd.txt /etc/motd
RUN echo "cat /etc/motd" >> $HOME/.bashrc

RUN mkdir -p /maestro
VOLUME /maestro

# CMD [ "/usr/bin/bash"]
ADD maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]
