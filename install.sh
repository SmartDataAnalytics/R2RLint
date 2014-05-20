#!/bin/sh

MVNEXEC="mvn"
RESDIR="src/main/resources"


$MVNEXEC compile

if (! [ -d etc ])
  then
    mkdir etc
  fi

if [ -d "$RESDIR" ]
  then
    for PFILE in ../"$RESDIR"/*properties
      do
        if (! [ -f etc/"$PFLIE" ])
          then
            cd etc
            ln -s ../"$RESDIR"/*properties .
            cd - > /dev/null
          fi
      done
  fi

