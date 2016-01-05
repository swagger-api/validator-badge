FROM java:8

WORKDIR /validator
COPY target/lib/jetty-runner* /validator/jetty-runner.jar
COPY target/*.war /validator/swagger-validator.war

RUN apt-get update

EXPOSE 8080
CMD ["java", "-jar", "/validator/jetty-runner.jar", "/validator/swagger-validator.war"]
