#!/bin/sh

MVNEXEC="mvn"
MAINCLS="org.aksw.sparqlify.qa.main.Run"
RESDIR="src/main/resources"
CLSDIR="target/classes/"

cp $RESDIR/metrics.properties $CLSDIR/metrics.properties

$MVNEXEC exec:java -Dexec.mainClass="$MAINCLS" -e
