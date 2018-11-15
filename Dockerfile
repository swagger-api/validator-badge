from openjdk:8-jre

WORKDIR /validator
RUN mkdir -p /validator/webapp/WEB-INF/lib

COPY target/lib/jetty-runner* /validator/jetty-runner.jar
COPY bin/run.sh /validator/

COPY src/main/webapp /validator/webapp

COPY target/lib/* /validator/webapp/WEB-INF/lib/
RUN rm /validator/webapp/WEB-INF/lib/jetty*
COPY target/classes /validator/webapp/WEB-INF/classes


EXPOSE 8080
CMD ["bash", "/validator/run.sh"]
