#!/bin/bash

if [ ! -f "${DIR}/env.sh" ]
then
	sudo cat > "${DIR}/env.sh" <<- EOF
		#!/bin/bash
		export JRM_SERVER_HTTP=8080
		export JRM_SERVER_HTTPS=8443
		export JRM_SERVER_WORKPATH="${HOME}/.jrommanager"
		export JRM_SERVER_SERVICE_USER="${USER:-root}"
		export JRM_SERVER_SERVICE_LOG="${DIR}/logs/service.log"
		export JRM_SERVER_SERVICE_ERR="${DIR}/logs/service.err"
EOF
	sudo chmod 700 "${DIR}/env.sh"
fi
