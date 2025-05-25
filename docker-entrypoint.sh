#!/bin/sh

#Automaticly import system proxy settings
if [ -n "$http_proxy" ] ; then
    echo $http_proxy | grep "@"
    if [ $? -eq 0 ]; then # If variable has username and password, its parse method different
        PROXY_HOST=$(echo $http_proxy | sed 's/http:\/\/.*@\(.*\):.*/\1/')
        PROXY_PORT=$(echo $http_proxy | sed 's/http:\/\/.*@.*:\(.*\)/\1/' | tr -d "/")
        USERNAME=$(echo $http_proxy | sed 's/http:\/\/\(.*\)@.*/\1/'|awk -F: '{print $1}')
        PASSWORD=$(echo $http_proxy | sed 's/http:\/\/\(.*\)@.*/\1/'|awk -F: '{print $2}')
    else # If it doesn't have username and password, its parse method this
        PROXY_HOST=$(echo $http_proxy | sed 's/http:\/\/\(.*\):.*/\1/')
        PROXY_PORT=$(echo $http_proxy | sed 's/http:\/\/.*:\(.*\)/\1/' | tr -d "/")
    fi
fi

java -Dhttp.proxyHost=$PROXY_HOST -Dhttp.proxyPort=$PROXY_PORT -Dhttp.proxyUser=$USERNAME -Dhttp.proxyPassword=$PASSWORD -Dhttp.nonProxyHosts=$no_proxy -jar -DswaggerUrl=swagger.yaml -DrejectLocal=$REJECT_LOCAL -DrejectRedirect=$REJECT_REDIRECT /validator/jetty-runner.jar /validator/server.war