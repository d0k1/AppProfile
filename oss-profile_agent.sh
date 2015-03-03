#!/bin/bash

./reset.sh

CMD="-Dagent.config=agent.properties -classpath example/target/example-1.0-SNAPSHOT.jar:jassie/src/main/resources/bond.jar:jassie/target/jassie.jar:/usr/lib/jvm/java-8-oracle/lib/tools.jar com.focusit.agent.example.profiler.HeavyLoadProfilerExample"

echo ${CMD}

sudo /opt/oracle-studio/bin/amd64/collect -o oss-expirement.1.er -d /tmp -p high -S on -h on -j on java ${CMD} > result.txt