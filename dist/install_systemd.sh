#!/bin/bash

DIR="$(dirname -- "$(readlink -f -- "$0")")"

pushd "$DIR" >/dev/null

source check_systemd.sh
source check_java.sh
source create_env.sh

sudo cat > start_service.sh << EOF
#!/bin/bash
source "${DIR}/env.sh"
mkdir -p "\${JRM_SERVER_WORKPATH}"
mkdir -p "\${JRM_SERVER_WORKPATH}/logs"
chown -R \${JRM_SERVER_SERVICE_USER} "\${JRM_SERVER_WORKPATH}"
touch "\${JRM_SERVER_SERVICE_LOG}"
chown \${JRM_SERVER_SERVICE_USER} "\${JRM_SERVER_SERVICE_LOG}"
touch "\${JRM_SERVER_SERVICE_ERR}"
chown \${JRM_SERVER_SERVICE_USER} "\${JRM_SERVER_SERVICE_ERR}"
"${DIR}/jsvc" -cwd "${DIR}" -home "${JAVA_HOME}" -user \${JRM_SERVER_SERVICE_USER} -cp JRomManager.jar -pidfile /var/run/JRomManager.pid -procname JRomManager -outfile "\${JRM_SERVER_SERVICE_LOG}" -errfile "\${JRM_SERVER_SERVICE_ERR}" -Dfile.encoding=UTF-8 jrm.server.Server
EOF

sudo chmod 755 start_service.sh

sudo cat > stop_service.sh << EOF
#!/bin/bash
source "${DIR}/env.sh"
"${DIR}/jsvc" -stop -cwd "${DIR}" -home "${JAVA_HOME}" -user \${JRM_SERVER_SERVICE_USER} -pidfile /var/run/JRomManager.pid -cp JRomManager.jar jrm.server.Server
EOF

sudo chmod 755 stop_service.sh

source create_service.sh

popd >/dev/null
