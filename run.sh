#!/bin/sh

echo "Running standalone web interface..."

./build.sh

java -classpath build/classes WebUI.WebServer
