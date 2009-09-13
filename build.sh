#!/bin/sh

rm -rf build/classes

javac \
	-classpath deps/freenet-testing-build-1233-pre1-snapshot.jar \
	-sourcepath src \
	-d build/classes \
	src/WebUI/WebUIPlugin.java &&

[ -d "build/jar" ] || mkdir build/jar

jar cfm build/jar/WebUI.jar manifest.mf web -C build/classes . 
