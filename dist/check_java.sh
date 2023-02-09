#!/bin/bash

if type -p java; then
    echo found java executable in PATH
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo found java executable in JAVA_HOME     
    _java="$JAVA_HOME/bin/java"
else
    echo "no java"
    exit
fi

JAVA_VER=$("$_java" -version 2>&1 | sed -n ';s/.* version "\(.*\)\.\(.*\)\..*".*/\1\2/p;')
if [ "$JAVA_VER" -ge 170 ]
then
	echo "ok, java is 17 or newer"
else
	echo "java version is too old, you need at least java 17"
	exit
fi

JAVA_HOME="$(dirname -- "$(dirname -- "$(readlink -f "$(which "$_java")")")")"
