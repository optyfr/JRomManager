#!/bin/bash

if [ ! -f "${DIR}/env.sh" ]
then
	sudo cat > "${DIR}/env.sh" <<- EOF
		#!/bin/bash
		export JRM_SERVER_BIND=0.0.0.0
		export JRM_SERVER_HTTP=8080
		export JRM_SERVER_HTTPS=8443
		export JRM_SERVER_WORKPATH="${HOME}/.jrommanager"
		export JRM_SERVER_SERVICE_USER="${USER:-root}"
		export JRM_SERVER_SERVICE_LOG="\${JRM_SERVER_WORKPATH}/logs/service.log"
		export JRM_SERVER_SERVICE_ERR="\${JRM_SERVER_WORKPATH}/logs/service.err"
		export JRM_SERVER_DEBUG=true
		export JRM_SERVER_CONNLIMIT=50
		export JRM_SERVER_RATELIMIT=5
		export JRM_SERVER_MINTHREADS=12
		export JRM_SERVER_MAXTHREADS=200
		export JRM_SERVER_MAXTHREADS=200
		export JRM_SERVER_SESSIONTIMEOUT=300
EOF
	sudo chmod 700 "${DIR}/env.sh"
fi
