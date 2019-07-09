FROM openjdk:8-jre-alpine3.7

ARG semver

WORKDIR /opt

COPY build/libs/waes-ta-${semver}-all.jar /opt/waes-ta-all.jar

EXPOSE 8000-9000

ENTRYPOINT java -jar /opt/wta-ta-all.jar \
  -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
  -conf ${CONFIG}
