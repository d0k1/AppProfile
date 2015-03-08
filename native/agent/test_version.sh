#!/bin/bash

CMD="-Dagent.config=agent.properties -classpath ../../example/target/example-1.0-SNAPSHOT.jar:../../jassie/src/main/resources/bond.jar:../../jassie/target/jassie.jar:/usr/lib/jvm/java-8-oracle/lib/tools.jar com.focusit.agent.example.example01.JavaAppExample02"

echo ${CMD}

#/usr/lib/jvm/java-7-oracle/bin/java -d64 -Xcheck:jni -Xverify:all -agentpath:/home/doki/source/agent/native/agent/build/libagent.so ${CMD} 
/usr/lib/jvm/java-8-oracle/bin/java -agentpath:/home/doki/source/agent/native/agent/build/libagent.so -version

#> result.txt