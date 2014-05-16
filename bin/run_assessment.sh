#!/bin/sh

MVNEXEC="mvn"
MAINCLS="org.aksw.sparqlify.qa.main.Run"

$MVNEXEC exec:java -Dexec.mainClass="$MAINCLS"
