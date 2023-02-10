#!/bin/bash

DIR="$(dirname -- "$(readlink -f -- "$0")")"

pushd "$DIR" >/dev/null

sudo cat > JRomManager.service << EOF
[Unit]
Description=JRomManager
After=network-online.target remote-fs.target
Wants=network-online.target

[Service]
Type=forking
ExecStart="${DIR}/start_service.sh"
ExecStop="${DIR}/stop_service.sh"
TimeoutSec=1min

[Install]
WantedBy=multi-user.target
EOF

sudo chmod 755 "${DIR}/JRomManager.service"
sudo ln -sf "$DIR/JRomManager.service" /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable JRomManager.service
sudo systemctl start JRomManager.service

popd >/dev/null
