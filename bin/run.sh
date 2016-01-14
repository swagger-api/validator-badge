#!/bin/sh

set -e

# use default basePath if none supplied
if [ -z $SWAGGER_BASE_PATH ]; then SWAGGER_BASE_PATH="/*"
else
  SWAGGER_BASE_PATH="$SWAGGER_BASE_PATH/*"
fi

# make it safe for sed
BASE_PATH=$(sed -e 's,/,\\/,g' <<< $SWAGGER_BASE_PATH)

sed -i "s/<url-pattern>\/\*/<url-pattern>$BASE_PATH/g" /validator/webapp/WEB-INF/web.xml

java -jar /validator/jetty-runner.jar /validator/webapp
