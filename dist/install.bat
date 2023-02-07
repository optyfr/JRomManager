@echo off
setlocal enabledelayedexpansion enableextensions

set DIR=%~dp0

@FOR /f "delims=" %%i in ('where java') DO set javaPath=%%i
echo !javaPath!

call :file_name_from_path JavaBin "!javaPath!"
echo %JavaBin%

pushd "%JavaBin%\.."
set JavaHome=%CD%
popd
echo %JavaHome%

.\prunsrv.exe install JRomManager --Description="JRomManager" --Jvm="%JavaBin%\server\jvm.dll" --JvmOptions=-Dfile.encoding=UTF-8 --JavaHome="%JavaHome%" --LogPath="%DIR%\logs" --StdOutput=auto --StdError=auto --Classpath=JRomManager.jar --StartMode=jvm --StartClass=jrm.server.Server --StartMethod=windowsService --StartParams=start;--workpath="%HOMEPATH%\.jrommanager" --StartPath=%DIR% --StopMode=jvm --StopClass=jrm.server.Server --StopMethod=windowsService --StopParams=stop --StopPath=%DIR%

goto :eof

:file_name_from_path <resultVar> <pathVar>
(
    set "%~1=%~dp2"
    exit /b
)

:eof
endlocal
