from openjdk:8-jre

WORKDIR /validator
COPY target/lib/jetty-runner* /validator/jetty-runner.jar
COPY bin/run.sh /validator/
ADD target/swagger-validator-1.0.5-SNAPSHOT /validator/webapp

RUN apt-get update

EXPOSE 8080
CMD ["bash", "/validator/run.sh"]
