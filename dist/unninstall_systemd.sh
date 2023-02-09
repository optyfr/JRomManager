#!/bin/bash

if [ ! -d /run/systemd/system ]
then
	echo "Your OS does not use systemd";
	exit
fi

sudo systemctl stop JRomManager.service
sudo systemctl disable JRomManager.service
sudo rm /etc/systemd/system/JRomManager.service
