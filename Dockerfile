FROM java:7

WORKDIR /validator
COPY . /validator

RUN apt-get update && \
    apt-get install -y maven2 && \
    mvn package

EXPOSE 8002
CMD ["mvn", "jetty:run"]
