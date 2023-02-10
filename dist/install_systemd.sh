#!/bin/bash

DIR="$(dirname -- "$(readlink -f -- "$0")")"

pushd "$DIR" >/dev/null

source check_systemd.sh
source check_java.sh
source create_env.sh

cat > start_service.sh << EOF
#!/bin/bash
source "${DIR}/env.sh"
"${DIR}/jsvc" -cwd "${DIR}" -home "${JAVA_HOME}" -user ${USER} -cp JRomManager.jar -pidfile /var/run/JRomManager.pid -procname JRomManager -outfile "${DIR}/logs/JRomManager.log" -errfile "${DIR}/logs/JRomManager.err" -Dfile.encoding=UTF-8 jrm.server.Server
EOF

sudo chown root:root start_service.sh
sudo chmod 755 start_service.sh

cat > stop_service.sh << EOF
#!/bin/bash
source "${DIR}/env.sh"
"${DIR}/jsvc" -stop -cwd "${DIR}" -home "${JAVA_HOME}" -user ${USER} -pidfile /var/run/JRomManager.pid -cp JRomManager.jar jrm.server.Server
EOF

sudo chown root:root stop_service.sh
sudo chmod 755 stop_service.sh

source create_service.sh

popd >/dev/null
