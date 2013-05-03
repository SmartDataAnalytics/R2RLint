#!/bin/bash

# Default Settings
defaultPort=5533


#DIR="$( cd "$( dirname "$0" )" && pwd )"
DIR=`pwd`

configDirArg="$1"

if [[ "${configDirArg:0:1}" == "/" ]]; then
	configDir="$configDirArg"
else
	configDir="$DIR/$configDirArg"
fi


port="${2:-$defaultPort}"
contextPath="${3:-/}"

cd "$DIR/../../linkedgeodata-usertags"

echo "Starting OSM User Tags Service"
echo "------------------------------"
echo "Port  : $port"
echo "Config: $configDir"
echo "------------------------------"

mvn jetty:run "-Djetty.port=$port" "-Dsml-eval.contextPath=$contextPath" "-DconfigDirectory=$configDir" 


