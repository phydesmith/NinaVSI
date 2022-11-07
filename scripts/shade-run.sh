#!/bin/bash
# Build via maven and shade and run via java

#mvn clean compile package shade:shade
mvn compile package shade:shade
if [[ $? -eq 0 ]] ;
  then
    java -classpath target/NinaVSI-1.0-SNAPSHOT.jar io.javasmithy.AppEntry
  else
    echo $?
    echo "Build Failed, no run."
fi