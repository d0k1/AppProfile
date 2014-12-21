#!/bin/bash
./reset.sh

mvn clean install -DskipTests

cp jassie/src/main/resources/bond.jar /tmp
#cp agent.properties /tmp