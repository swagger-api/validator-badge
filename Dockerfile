FROM openjdk:8-jre-alpine

WORKDIR /validator

COPY target/lib/jetty-runner.jar /validator/jetty-runner.jar
COPY target/*.war /validator/server.war
COPY src/main/swagger/swagger.yaml /validator/
COPY inflector.yaml /validator/

EXPOSE 8080

CMD ["java", "-jar", "-DswaggerUrl=swagger.yaml", "/validator/jetty-runner.jar", "/validator/server.war"]

