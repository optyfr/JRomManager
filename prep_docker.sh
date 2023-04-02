#!/bin/bash

rm -rf build/docker
mkdir build/docker
mkdir build/docker/lib
rsync -a -c build/install/JRomManager/lib/ build/docker/lib/
rsync -a -c build/install/JRomManager/*.jar build/docker/
cp docker/Dockerfile build/docker/Dockerfile
