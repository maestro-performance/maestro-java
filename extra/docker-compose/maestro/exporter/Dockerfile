FROM centos:7 AS buildimg
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
ARG MAESTRO_VERSION
ENV MAESTRO_VERSION ${MAESTRO_VERSION:-1.3.7}
ARG MAESTRO_BRANCH
ENV MAESTRO_BRANCH ${MAESTRO_BRANCH:-devel}
LABEL MAESTRO_VERSION=${MAESTRO_VERSION}
RUN yum install -y java-1.8.0-openjdk-devel git which unzip zip ntp
ENV JAVA_HOME /etc/alternatives/jre
WORKDIR /root/build
RUN git clone https://github.com/maestro-performance/maestro-java.git --branch ${MAESTRO_BRANCH}
RUN cd maestro-java && ./mvnw -DskipTests=true -DskipTests=true -PPackage clean package

FROM centos:7 AS maestro-exporter
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
ARG MAESTRO_VERSION
ENV MAESTRO_VERSION ${MAESTRO_VERSION:-1.3.7}
LABEL MAESTRO_VERSION=${MAESTRO_VERSION}
ENV MAESTRO_APP_ROOT /opt/maestro
EXPOSE 8000
RUN mkdir -p ${MAESTRO_APP_ROOT}
WORKDIR ${MAESTRO_APP_ROOT}
RUN yum install -y java-1.8.0-openjdk ntp
RUN yum clean all
COPY --from=buildimg /root/build/maestro-java/maestro-exporter/target/maestro-exporter-${MAESTRO_VERSION}-bin.tar.gz .
RUN mkdir maestro-exporter && tar --strip-components=1 -xvf maestro-exporter-${MAESTRO_VERSION}-bin.tar.gz -C maestro-exporter
RUN rm -f maestro-exporter-${MAESTRO_VERSION}-bin.tar.gz
RUN mkdir -p /maestro
VOLUME /maestro

ADD log4j.properties ${MAESTRO_APP_ROOT}/maestro-exporter/config/log4j.properties
ADD maestro-exporter.properties ${MAESTRO_APP_ROOT}/maestro-exporter/config/maestro-exporter.properties
RUN mkdir -p /maestro/
VOLUME /maestro/
RUN mkdir -p /maestro/exporter/logs
ADD maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]
