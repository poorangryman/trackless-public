@echo off
setlocal
cd /d "%~dp0"

if "%JAVA_HOME%"=="" (
  if exist "C:\Users\%USERNAME%\.jdks\jbr-17.0.14" set "JAVA_HOME=C:\Users\%USERNAME%\.jdks\jbr-17.0.14"
)

set /p VERSION=<VERSION.txt
for /f "tokens=* delims= " %%a in ("%VERSION%") do set VERSION=%%a

echo ========================================
echo TrackLess %VERSION% - release APK build
echo ========================================
echo.
call gradlew.bat assembleRelease --no-daemon
if errorlevel 1 (
  echo.
  echo BUILD FAILED.
  pause
  exit /b 1
)
echo.
echo BUILD OK.
echo Final APK:
echo app\build\outputs\apk\release\TrackLess-v%VERSION%.apk
pause
