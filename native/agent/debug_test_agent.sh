#!/bin/bash

CMD="-Dagent.config=agent.properties -classpath ../../example/target/example-1.0-SNAPSHOT.jar:../../jassie/src/main/resources/bond.jar:../../jassie/target/jassie.jar:/usr/lib/jvm/java-8-oracle/lib/tools.jar com.focusit.agent.example.example01.JavaAppExample02"

echo ${CMD}

/home/doki/source/jdk8u-dev/build/linux-x86_64-normal-server-slowdebug/jdk/bin/java -d64 -Xcheck:jni -Xverify:all -agentpath:/home/doki/source/agent/native/agent/build/libbond.so ${CMD} 

#> result.txt