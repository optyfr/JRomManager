#!/bin/bash

DIR="$(dirname -- "$(readlink -f -- "$0")")"

source "$DIR/check_systemd.sh"

sudo systemctl stop JRomManager.service
sudo systemctl disable JRomManager.service
