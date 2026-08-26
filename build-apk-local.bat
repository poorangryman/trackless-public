@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo TrackLess 1.2.4 - release APK build
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
echo app\build\outputs\apk\release\TrackLess-v1.2.4.apk
pause
