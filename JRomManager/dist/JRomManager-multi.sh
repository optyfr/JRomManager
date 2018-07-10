#!/bin/sh
dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
java -Xmx1g -jar JRomManager.jar --multiuser --noupdate&
