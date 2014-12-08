#!/bin/bash
rm ./methods.data
rm ./profile.data

mvn clean install -DskipTests
