#!/bin/bash
APP_NAME=olive-api-exec.jar
APP_HOME=$(cd "$(dirname "$0")" && pwd)
echo $APP_HOME

JAVA_HOME=

java -cp "$APP_HOME/$APP_NAME:$APP_HOME/lib/*" com.olive.OliveApplication