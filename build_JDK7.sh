#!/bin/bash
./reset.sh

export JAVA_HOME=/usr/lib/jvm/java-7-oracle
mvn clean install -DskipTests

cp jassie/src/main/resources/bond.jar /tmp
cp agent.properties /tmp