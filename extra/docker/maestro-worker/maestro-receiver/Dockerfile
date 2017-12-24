FROM maestro-worker
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
EXPOSE 8000
ENV MAESTRO_APP_ROOT /opt/maestro
WORKDIR ${MAESTRO_APP_ROOT}
ADD log4j.properties ${MAESTRO_APP_ROOT}/maestro-worker/config/log4j.properties
RUN mkdir -p /maestro/
VOLUME /maestro/
RUN mkdir -p /maestro/receiver/logs
ADD maestro-container-wrapper.sh /usr/bin/maestro-container-wrapper
# RUN /usr/bin/maestro-data-server
# CMD [ "sh", "-c", "./maestro-worker/bin/maestro-worker -m mqtt://maestro-broker:1883 -w net.orpiske.mpt.maestro.worker.jms.JMSReceiverWorker -l /maestro/receiver/logs -r receiver -H maestro-receiver" ] 
CMD [ "sh", "-c", "/usr/bin/maestro-container-wrapper"]