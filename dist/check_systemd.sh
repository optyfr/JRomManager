#!/bin/bash

if [ ! -d /run/systemd/system ]
then
	echo "Your OS does not use systemd";
	exit
fi
