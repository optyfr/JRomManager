#!/bin/sh
\nwhile\n    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]\ndo
        ls=$( ls -ld "$app_path" )
