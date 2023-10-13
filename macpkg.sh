#! /usr/bin/env bash
set -eu
JAR=CHANGE-IT-1.0.0-SNAPSHOT.jar
NAME=CHANGE-IT
ICON=CHANGE-IT.icns
rm -rf pkg
mkdir pkg
mvn clean package
cp target/$JAR pkg/
#cp splash.png pkg/
cp LICENCE.TXT pkg/
/Users/fq/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home/bin/jpackage --verbose --name $NAME --icon $ICON --input ./pkg --main-jar $JAR 
