if [ ! -f "${DIR}/env.sh" ]
then
	mkdir -p ${HOME}/.jrommanager
	sudo cat > "${DIR}/env.sh" <<- EOF
		export JRM_SERVER_HTTP=8080
		export JRM_SERVER_HTTPS=8443
		export JRM_SERVER_WORKPATH="${HOME}/.jrommanager"
EOF
	sudo chmod 700 "${DIR}/env.sh"
fi
