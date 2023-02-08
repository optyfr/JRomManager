#!/bin/bash
DIR="$(dirname -- "$(readlink -f -- "$0")")"

JAVA_HOME="$(dirname -- "$(dirname -- "$(readlink -f "$(which java)")")")"

cat > "$DIR/JRomManager.service" << EOF
[Unit]
Description=JRomManager
After=network-online.target remote-fs.target
Wants=network-online.target

[Service]
Type=forking
ExecStart=$DIR/jsvc -cwd "$(DIR)" -home "$(JAVA_HOME)" -cp JRomManager.jar -pidfile /var/run/JRomManager.pid -procname JRomManager -outfile "$(DIR)/logs/JRomManager.log" -errfile "$(DIR)/logs/JRomManager.err" jrm.server.Server
ExecStop=$DIR/jsvc -stop -cwd "$(DIR)" -home "$(JAVA_HOME)" -pidfile /var/run/JRomManager.pid -cp JRomManager.jar jrm.server.Server
TimeoutSec=1min

[Install]
WantedBy=multi-user.target
EOF

