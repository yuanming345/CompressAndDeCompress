#!/bin/bash
mainclass="com.MyTestIntergerTest"
 LANG="en_US.UTF-8"
APP_HOME=$(cd "$(dirname "$0")";cd ..; pwd)
cd $APP_HOME
CLASSPATH=$APP_HOME/lib
for i in "$APP_HOME"/lib/*.jar
do
CLASSPATH="$CLASSPATH":"$i"
done

ps -ef | grep $mainclass | grep -v grep | awk '{print $2}' | xargs kill >/dev/null 2>&1
if [ -f server.out ];then
	cat /dev/null > server.out
fi;

#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=12345"
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
#JAVA_OPTS="$JAVA_OPTS -XX:+UnlockCommercialFeatures -XX:+FlightRecorder "
JAVA_OPTS="$JAVA_OPTS -Xmx1000m"
 (java $JAVA_OPTS   $mainclass >server.out 2>&1 &)