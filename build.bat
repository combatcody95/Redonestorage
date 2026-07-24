@echo off
setlocal

rem Always run relative commands from the folder containing this script.
pushd "%~dp0" || (
  echo Could not open the project folder: %~dp0
  exit /b 1
)

where java >nul 2>nul || (
  echo Java was not found. Install a 64-bit Java 21 JDK and add it to PATH.
  popd
  exit /b 1
)

set "VALIDATED=0"
py -3 --version >nul 2>nul
if not errorlevel 1 (
  echo Checking source resources with Python...
  py -3 "%CD%\tools\validate_port.py"
  if errorlevel 1 (
    set "EXITCODE=%ERRORLEVEL%"
    popd
    exit /b %EXITCODE%
  )
  set "VALIDATED=1"
)

if "%VALIDATED%"=="0" (
  python --version >nul 2>nul
  if not errorlevel 1 (
    echo Checking source resources with Python...
    python "%CD%\tools\validate_port.py"
    if errorlevel 1 (
      set "EXITCODE=%ERRORLEVEL%"
      popd
      exit /b %EXITCODE%
    )
    set "VALIDATED=1"
  )
)

if "%VALIDATED%"=="0" (
  echo Python was not found. Skipping the optional source-resource validation.
)

echo Building Redone Storage for Minecraft 1.21.1 / NeoForge 21.1.234...
call "%CD%\gradlew.bat" clean build
set "EXITCODE=%ERRORLEVEL%"
if not "%EXITCODE%"=="0" (
  popd
  exit /b %EXITCODE%
)

echo.
echo Build complete. Check "%CD%\build\libs\"
popd
exit /b 0
