FROM maven:3.6-jdk-8-slim as build

WORKDIR /root

ENV MTA_CLI_BUILD_PATH=/root/target/mta-cli/mta-cli-5.1.3.Final

COPY script/unpack-mta-cli.xml unpack-mta-cli.xml
RUN mvn compile -f unpack-mta-cli.xml

COPY tcd-windup tcd-windup
RUN mvn install -DskipTests -f tcd-windup/pom.xml

RUN ${MTA_CLI_BUILD_PATH}/bin/mta-cli --batchMode --install io.tackle:tcd-windup:0.0.1-SNAPSHOT

RUN curl -k -L -o janusgraph-0.3.2-hadoop2.zip https://github.com/JanusGraph/janusgraph/releases/download/v0.3.2/janusgraph-0.3.2-hadoop2.zip
RUN jar xvf janusgraph-0.3.2-hadoop2.zip

FROM openjdk:8u282-jre-slim

RUN apt-get update
RUN apt-get install -y git python3 python3-pip
RUN pip3 install Flask

ENV UID=185

RUN useradd -m -u ${UID} tcd
USER ${UID}

ENV HOME=/home/tcd

WORKDIR ${HOME}

COPY script script

ENV MTA_CLI_BUILD_PATH=/root/target/mta-cli/mta-cli-5.1.3.Final

ENV JANUSGRAPH_PATH=${HOME}/janusgraph-0.3.2-hadoop2
ENV TCD_APPLICATION_PATH=${HOME}/app
ENV MTA_CLI_PATH=${HOME}/mta-cli

COPY --from=build /root/.mta ${HOME}/.mta
COPY --from=build ${MTA_CLI_BUILD_PATH} ${MTA_CLI_PATH}
COPY --from=build /root/janusgraph-0.3.2-hadoop2 janusgraph-0.3.2-hadoop2

USER root

RUN chown -R ${UID} ${HOME}

USER ${UID}

RUN sed -i janusgraph-0.3.2-hadoop2/conf/gremlin-server/gremlin-server.yaml -e 's/conf\/gremlin-server\/janusgraph-cql-es-server\.properties/\/home\/tcd\/script\/TitanConfiguration\.properties/g'
RUN sed -i janusgraph-0.3.2-hadoop2/conf/gremlin-server/gremlin-server.yaml -e 's/scripts\/empty-sample\.groovy/\/home\/tcd\/janusgraph-0\.3\.2-hadoop2\/scripts\/empty-sample\.groovy/g'

RUN chmod a+x ${HOME}/script/start.sh
RUN chmod a+x ${HOME}/script/run.py
RUN chmod a+x ${JANUSGRAPH_PATH}/bin/*.sh

ENTRYPOINT ["script/run.py"]
