FROM java:8

WORKDIR /validator
COPY . /validator

RUN apt-get update && \
    apt-get install -y maven && \
    mvn package

EXPOSE 8002
CMD ["mvn", "jetty:run"]
