@echo off
setlocal
cd /d "%~dp0"

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
