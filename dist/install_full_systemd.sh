#!/bin/bash

DIR="$(dirname -- "$(readlink -f -- "$0")")"

source "$DIR/check_systemd.sh"
source "$DIR/check_java.sh"
source "$DIR/create_env.sh"

pushd "$DIR"



cat > JRomManager.service << EOF
[Unit]
Description=JRomManager
After=network-online.target remote-fs.target
Wants=network-online.target

[Service]
EnvironmentFile="${DIR}/env.sh"
Type=forking
ExecStart="${DIR}/jsvc" -cwd "${DIR}" -home "${JAVA_HOME}" -user ${USER} -cp JRomManager.jar -pidfile /var/run/JRomManager.pid -procname JRomManager -outfile "${DIR}/logs/JRomManager.log" -errfile "${DIR}/logs/JRomManager.err" -Dfile.encoding=UTF-8 jrm.fullserver.FullServer
ExecStop="${DIR}/jsvc" -stop -cwd "${DIR}" -home "${JAVA_HOME}" -user ${USER} -pidfile /var/run/JRomManager.pid -cp JRomManager.jar jrm.fullserver.FullServer
TimeoutSec=1min

[Install]
WantedBy=multi-user.target
EOF

sudo ln -sf "$DIR/JRomManager.service" /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable JRomManager.service
sudo systemctl start JRomManager.service

popd
