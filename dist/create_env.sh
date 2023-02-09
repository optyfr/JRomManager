if [ ! -f "${DIR}/env.sh" ]
then
	mkdir -p ${HOME}/.jrommanager
	cat > "${DIR}/env.sh" <<- EOF
		JRM_SERVER_HTTP=8080
		JRM_SERVER_HTTPS=8443
		JRM_SERVER_WORKPATH=${HOME}/.jrommanager
EOF
fi
