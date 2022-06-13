@echo off
setlocal

rem
rem Copyright (c) 1999, 2021 Tanuki Software, Ltd.
rem http://www.tanukisoftware.com
rem All rights reserved.
rem
rem This software is the proprietary information of Tanuki Software.
rem You shall use it only in accordance with the terms of the
rem license agreement you entered into with Tanuki Software.
rem http://wrapper.tanukisoftware.com/doc/english/licenseOverview.html
rem
rem Java Service Wrapper command based script.
rem

rem -----------------------------------------------------------------------------
rem These settings can be modified to fit the needs of your application
rem Optimized for use with version 3.5.45 of the Wrapper.

rem The base name for the Wrapper binary.
set _WRAPPER_BASE=wrapper

rem The directory where the Wrapper binary (.exe) file is located. It can be
rem  either an absolute or a relative path. If the path contains any special 
rem  characters, please make sure to quote the variable. 
set _WRAPPER_DIR=

rem The name and location of the Wrapper configuration file. This will be used
rem  if the user does not specify a configuration file as the first parameter to
rem  this script.
set _WRAPPER_CONF="../conf/%_WRAPPER_BASE%.conf"

rem _FIXED_COMMAND tells the script to use a hard coded command rather than
rem  expecting the first parameter of the command line to be the command.
rem  By default the command will will be expected to be the first parameter.
rem set _FIXED_COMMAND=console

rem _PASS_THROUGH controls how the script arguments should be passed to the
rem  Wrapper. Possible values are:
rem  - commented or 'false': the arguments will be ignored (not passed).
rem  - 'app_args' or 'true': the arguments will be passed through the Wrapper as
rem                          arguments for the Java Application.
rem  - 'both': both Wrapper properties and Application arguments can be passed to
rem            the Wrapper. The Wrapper properties come in first position. The
rem            user can optionally add a '--' separator followed by application
rem            arguments.
rem NOTE - If _FIXED_COMMAND is set to true the above applies to all arguments,
rem        otherwise it applies to arguments starting with the second.
rem NOTE - Passing arguments is only valid with the 'console', 'install',
rem        'installstart' and 'update' commands.
set _PASS_THROUGH=app_args

rem If there are any errors, the script will pause for a specific number of seconds
rem  or until the user presses a key. (0 not to wait, negative to wait forever).
set _WRAPPER_TIMEOUT=-1

rem Do not modify anything beyond this point
rem -----------------------------------------------------------------------------

if "%OS%"=="Windows_NT" goto nt
echo This script only works with NT-based versions of Windows.
goto :eof

:nt
rem Find the application home.
rem if no path path specified do the default action
IF not DEFINED _WRAPPER_DIR goto dir_undefined
set _WRAPPER_DIR_QUOTED="%_WRAPPER_DIR:"=%"
if not "%_WRAPPER_DIR:~-2,1%" == "\" set _WRAPPER_DIR_QUOTED="%_WRAPPER_DIR_QUOTED:"=%\"
rem check if absolute path
if "%_WRAPPER_DIR_QUOTED:~2,1%" == ":" goto absolute_path
if "%_WRAPPER_DIR_QUOTED:~1,1%" == "\" goto absolute_path
rem everythig else means relative path
set _REALPATH="%~dp0%_WRAPPER_DIR_QUOTED:"=%"
goto pathfound

:dir_undefined
rem Use a relative path to the wrapper %~dp0 is location of current script under NT
set _REALPATH="%~dp0"
goto pathfound
:absolute_path
rem Use an absolute path to the wrapper
set _REALPATH="%_WRAPPER_DIR_QUOTED:"=%"

:pathfound
rem
rem Decide on the specific Wrapper binary to use (See delta-pack)
rem
if "%PROCESSOR_ARCHITEW6432%"=="AMD64" goto amd64
if "%PROCESSOR_ARCHITECTURE%"=="AMD64" goto amd64
if "%PROCESSOR_ARCHITECTURE%"=="IA64" goto ia64
:x86_32
set _WRAPPER_L_EXE="%_REALPATH:"=%%_WRAPPER_BASE%-windows-x86-32.exe"
set _BIN_BITS="32"
goto search
:amd64
set _WRAPPER_L_EXE="%_REALPATH:"=%%_WRAPPER_BASE%-windows-x86-64.exe"
set _BIN_BITS="64"
goto search
:ia64
set _WRAPPER_L_EXE="%_REALPATH:"=%%_WRAPPER_BASE%-windows-ia-64.exe"
set _BIN_BITS="64"
goto search
:search
set _WRAPPER_EXE="%_WRAPPER_L_EXE:"=%"
if exist %_WRAPPER_EXE% goto check_lic_bits
set _WRAPPER_EXE="%_REALPATH:"=%%_WRAPPER_BASE%.exe"
if exist %_WRAPPER_EXE% goto conf
if %_BIN_BITS%=="64" goto x86_32
echo Unable to locate a Wrapper executable using any of the following names:
echo %_WRAPPER_L_EXE%
echo %_WRAPPER_EXE%
goto preexitpause

:check_lic_bits
rem The command should not be called inside a IF, else errorlevel would be 0
if not %_BIN_BITS%=="64" goto conf
%_WRAPPER_EXE% --request_delta_binary_bits %_WRAPPER_CONF% > nul 2>&1
if %errorlevel% equ 32 (
    set _LIC32_OS64=true
    goto x86_32
)

:conf
if [%_PASS_THROUGH%]==[true] (
    set _PASS_THROUGH=app_args
)
if [%_PASS_THROUGH%]==[app_args] (
    set _PARAMETERS=--
    set ARGS_ARE_APP_PARAMS=true
    set _PASS_THROUGH_ON=true
)
if [%_PASS_THROUGH%]==[both] (
    set _PASS_THROUGH_ON=true
)
if not [%_PASS_THROUGH_ON%]==[true] (
    set _PASS_THROUGH=false
)

set _SCRIPT_NAME=%~n0

if not [%_FIXED_COMMAND%]==[] (
    set _COMMAND=%_FIXED_COMMAND%
) else (
    set _COMMAND=%1
    shift
)

rem Check the command
if [%_COMMAND%]==[console]      goto args_allowed
if [%_COMMAND%]==[setup]        goto args_not_allowed
if [%_COMMAND%]==[teardown]     goto args_not_allowed
if [%_COMMAND%]==[start]        goto args_not_allowed
if [%_COMMAND%]==[stop]         goto args_not_allowed
if [%_COMMAND%]==[install]      goto args_allowed
if [%_COMMAND%]==[installstart] goto args_allowed
if [%_COMMAND%]==[pause]        goto args_not_allowed
if [%_COMMAND%]==[resume]       goto args_not_allowed
if [%_COMMAND%]==[status]       goto args_not_allowed
if [%_COMMAND%]==[remove]       goto args_not_allowed
if [%_COMMAND%]==[restart]      goto args_not_allowed

rem The command is invalid
if [%_FIXED_COMMAND%]==[] (
    echo Unexpected command: %_COMMAND%
    echo.
    goto showusage
) else (
    echo Invalid value '%_COMMAND%' for _FIXED_COMMAND.
    goto preexitpause
)

:args_not_allowed
if not [%1]==[] (
    echo Additional arguments are not allowed with the %_COMMAND% command.
    if not [%_FIXED_COMMAND%]==[] (
        rem The command can't be used with PASS_THROUGH, so disable it to show appropriate usage.
        set _PASS_THROUGH=false
    )
    goto showusage
)

:args_allowed
if not [%1]==[] (
    if [%_PASS_THROUGH%]==[false] (
        echo Additional arguments are not allowed when _PASS_THROUGH is set to false.
        goto preexitpause
    )
)

rem Collect all parameters
:parameters
if [%1]==[] goto callcommand
if [%ARGS_ARE_APP_PARAMS%]==[true] goto append
if [%1]==[--] (
    set ARGS_ARE_APP_PARAMS=true
    goto append
)
rem So we are appending a wrapper property.
rem   1) Check it is wrapped inside double quotes.
if not ["%~1"]==[%1] (
    if not [%_MISSING_QUOTES_REPORTED%]==[true] (
        set _MISSING_QUOTES_REPORTED=true
        echo WARNING: Any property assignment before '--' should be wrapped inside double quotes on Windows. In a powershell prompt command, double quotes should be escaped with backquote characters ^(^`^).
    )
    rem If not wrapped inside quotes, the following tests are not relevant, so skip them. Should we stop? We always used to continue.. but the Wrapper will probably fail.
    goto append
)
rem   2) Check that the arg matches the pattern of a property (the command should be outside of a IF block for errorlevel to be correct)
echo %1 | findstr ^wrapper\..*\=.*$ > nul 2>&1
if %errorlevel% equ 0 goto append
echo %1 | findstr ^.*\=.*$ > nul 2>&1
if %errorlevel% equ 0 goto unkown_property
rem Not a valid assignment.
echo WARNING: Encountered an invalid configuration property assignment '%~1'. When PASS_THROUGH is set to 'both', any argument before '--' should be in the format '^<property_name^>=^<value^>'.
goto append
:unkown_property
rem The property name is not starting with 'wrapper.' so invalid.
rem Extract the property name (this should be outside of a IF-ELSE block)
for /f "tokens=1* delims==" %%a in ("%~1") do set _COMMAND_PROP=%%a
echo WARNING: Encountered an unknown configuration property '%_COMMAND_PROP%'. When PASS_THROUGH is set to 'both', any argument before '--' should target a valid Wrapper configuration property.
:append
set _PARAMETERS=%_PARAMETERS% %1
shift
goto parameters

rem
rem Run the Wrapper
rem
:callcommand
if [%_COMMAND%]==[console] (
    %_WRAPPER_EXE% -c "%_WRAPPER_CONF%" %_PARAMETERS%
) else if [%_COMMAND%]==[setup] (
    %_WRAPPER_EXE% -su "%_WRAPPER_CONF%"
) else if [%_COMMAND%]==[teardown] (
    %_WRAPPER_EXE% -td "%_WRAPPER_CONF%"
) else if [%_COMMAND%]==[start] (
    call :start
) else if [%_COMMAND%]==[stop] (
    call :stop
) else if [%_COMMAND%]==[install] (
    %_WRAPPER_EXE% -i "%_WRAPPER_CONF%" %_PARAMETERS%
) else if [%_COMMAND%]==[installstart] (
    %_WRAPPER_EXE% -it "%_WRAPPER_CONF%" %_PARAMETERS%
) else if [%_COMMAND%]==[pause] (
    %_WRAPPER_EXE% -a "%_WRAPPER_CONF%"
) else if [%_COMMAND%]==[resume] (
    %_WRAPPER_EXE% -e "%_WRAPPER_CONF%"
) else if [%_COMMAND%]==[status] (
    %_WRAPPER_EXE% -q "%_WRAPPER_CONF%"
) else if [%_COMMAND%]==[remove] (
    %_WRAPPER_EXE% -r "%_WRAPPER_CONF%"
) else if [%_COMMAND%]==[restart] (
   call :stop
   call :start
)
if not errorlevel 1 goto :eof
goto preexitpause

:start
    %_WRAPPER_EXE% -t "%_WRAPPER_CONF%"
    goto :eof
:stop
    %_WRAPPER_EXE% -p "%_WRAPPER_CONF%"
    goto :eof

:showusage
if [%_PASS_THROUGH%]==[app_args] (
    set ARGS= {JavaAppArgs}
) else if [%_PASS_THROUGH%]==[both] (
    set ARGS= {WrapperProperties} [-- {JavaAppArgs}]
) else (
    set ARGS=
)

if [%_FIXED_COMMAND%]==[] (
    echo Usage: %_SCRIPT_NAME% [ console%ARGS% : start : stop : restart : pause : resume : status : install%ARGS% : installstart%ARGS% : remove : setup : teardown ]
    echo.
    echo Commands:
    echo   console      Launch in the current console.
    echo   start        Start the Service.
    echo   stop         Stop the Service.
    echo   restart      Stop the Service if running and then start.
    echo   pause        Pause the Service if running.
    echo   resume       Resume the Service if paused.
    echo   status       Query the current status of the Service.
    echo   install      Install the Service.
    echo   installstart Install the Service and then start running it.
    echo   remove       Uninstall the Service.
    echo   setup        Setup the Wrapper ^(for registration to the Windows Event Log^).
    echo   teardown     Teardown the Wrapper ^(unregister from the Windows Event Log^).
    echo.
) else (
    echo Usage: %_SCRIPT_NAME%%ARGS%
)
if not [%_PASS_THROUGH%]==[false] (
    if [%_PASS_THROUGH%]==[both] (
        echo WrapperProperties:
        echo   Optional configuration properties which will be passed to the Wrapper.
        echo.
    )
    echo JavaAppArgs:
    echo   Optional arguments which will be passed to the Java application.
    echo.
)

:preexitpause
if %_WRAPPER_TIMEOUT% gtr 0 (
    timeout /t %_WRAPPER_TIMEOUT%
) else (
    if %_WRAPPER_TIMEOUT% lss 0 (
        pause
    )
)

