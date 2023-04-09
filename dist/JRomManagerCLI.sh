#!/bin/sh
cd -P -- "$(dirname -- "$(readlink -f -- "$0")")"
OPT=-Xmx1g
which grep >/dev/null
if [ $? -eq 0 ]; then
	which file >/dev/null
	if [ $? -eq 0 ]; then
		file -L $(which java) | grep -q '64-bit'
		if [ $? -eq 0 ]; then OPT=-Xmx2g; fi
	else
		java --version | grep -q '64-Bit'
		if [ $? -eq 0 ]; then OPT=-xmx2g; fi
	fi
fi
java $OPT -cp "JRomManager.jar;lib/*"  jrm.cli.JRomManagerCLI
