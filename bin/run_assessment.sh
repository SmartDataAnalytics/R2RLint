#!/bin/sh

MVNEXEC="mvn"
MAINCLS="org.aksw.sparqlify.qa.main.Run"
RESDIR="src/main/resources"
CLSDIR="target/classes/"

cp $RESDIR/metrics.properties $CLSDIR/metrics.properties
cp $RESDIR/environment.properties $CLSDIR/environment.properties

$MVNEXEC exec:java -Dexec.mainClass="$MAINCLS" -e
