FROM eclipse-temurin:21.0.8_9-jdk

LABEL org.opencontainers.image.description "Docker image of JRomManager (with JDK 21)"
LABEL org.opencontainers.image.source https://github.com/optyfr/JRomManager

WORKDIR /app

COPY lib lib/
COPY *.jar ./

ENV JRM_SERVER_WORKPATH=/work
ENV JRM_SERVER_HTTP=8080
ENV JRM_SERVER_DEBUG=1

EXPOSE ${JRM_SERVER_HTTP}

VOLUME ["${JRM_SERVER_WORKPATH}"]

ENTRYPOINT ["java", "-cp", "JRomManager.jar", "jrm.server.Server"]
