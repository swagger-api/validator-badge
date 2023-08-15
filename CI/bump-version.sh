#!/bin/bash

CURRENT_VERSION="$(./CI/version.sh)"

echo "old version is: " ${CURRENT_VERSION}

NEW_VERSION=`python ./CI/nextVersion.py "${CURRENT_VERSION}"`

echo "pom.xml will be bumped from ${CURRENT_VERSION} to ${NEW_VERSION}"
mvn -q versions:set -DnewVersion="${NEW_VERSION}"
