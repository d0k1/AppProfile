#!/bin/bash
rm ./methods.data
rm ./profile.data
rm ./agent.log

mvn clean install -DskipTests
