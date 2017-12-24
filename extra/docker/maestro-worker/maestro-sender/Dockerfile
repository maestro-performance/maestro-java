FROM maestro-worker
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
EXPOSE 8000
ENV MAESTRO_APP_ROOT /opt/maestro
WORKDIR ${MAESTRO_APP_ROOT}
ADD log4j.properties ${MAESTRO_APP_ROOT}/maestro-worker/config/log4j.properties
RUN mkdir -p /maestro/
VOLUME /maestro/
RUN mkdir -p /maestro/sender/logs
#ADD maestro-data-server.sh /usr/bin/maestro-data-server
#RUN /usr/bin/maestro-data-server
#CMD [ "sh", "-c", "./maestro-worker/bin/maestro-worker -m mqtt://maestro-broker:1883 -w net.orpiske.mpt.maestro.worker.jms.JMSSenderWorker -l /maestro/sender/logs -r sender -H maestro-sender" ] 
ADD maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]
