#!/bin/bash

./reset.sh

CMD="-Dagent.config=/home/dkirpichenkov/SecretPath/agent.properties -Dagent.jar=/home/dkirpichenkov/SecretPath/jassie/src/main/resources/bond.jar -javaagent:/home/dkirpichenkov/SecretPath/jassie/src/main/resources/bond.jar -classpath example/target/example-1.0-SNAPSHOT.jar com.focusit.agent.example.example01.JavaAppExample02"

echo ${CMD}

java ${CMD} > result.txt