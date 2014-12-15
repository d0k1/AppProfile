#!/bin/bash

./reset.sh

CMD="-Dagent.config=agent.properties -Dagent.jar=jassie/src/main/resources/bond.jar -javaagent:jassie/src/main/resources/bond.jar -classpath example/target/example-1.0-SNAPSHOT.jar com.focusit.agent.example.example01.JavaAppExample02"

echo ${CMD}

java ${CMD} > result.txt