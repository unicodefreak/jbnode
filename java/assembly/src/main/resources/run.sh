#!/bin/bash

JAVACMD="java"
JAVA_OPTS="$JAVA_OPTS -Duser.language=en -Duser.region=us -Dfile.encoding=UTF-8 -Xmx512m"
STARTER_MAIN_CLASS=net.karmafiles.jbnode.Bootstrap

DIRNAME="`dirname $0`/lib"
EXT_DIRNAME="`dirname $0`/examples"

STARTER_CLASSPATH="";

if [ -d "$DIRNAME" ]; then
for i in `ls -d $DIRNAME/*.jar`; do
  STARTER_CLASSPATH="$STARTER_CLASSPATH":"$i"
done
fi

if [ -d "$EXT_DIRNAME" ]; then
for i in `ls -d $EXT_DIRNAME/*.jar`; do
  STARTER_CLASSPATH="$STARTER_CLASSPATH":"$i"
done
fi

exec "$JAVACMD" $JAVA_OPTS -classpath "$STARTER_CLASSPATH" "$STARTER_MAIN_CLASS" $@
