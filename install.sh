#!/bin/sh

MVNEXEC="mvn"
CLSDIR="target/classes"


$MVNEXEC compile

if (! [ -d etc ])
  then
    mkdir etc
  fi

if [ -d "$CLSDIR" ]
  then
    for PFILE in ../"$CLSDIR"/*properties
      do
        if (! [ -f etc/"$PFLIE" ])
          then
            cd etc
            ln -s ../"$CLSDIR"/*properties .
            cd - > /dev/null
          fi
      done
  fi

