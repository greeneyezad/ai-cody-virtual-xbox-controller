@echo off
setlocal
where adb >nul 2>nul
if errorlevel 1 (
  echo Android platform-tools are not installed or adb is not on PATH.
  exit /b 1
)
adb devices
adb reverse tcp:26760 tcp:26760
if errorlevel 1 (
  echo Failed to configure reverse tunnel. Authorize USB debugging on the phone.
  exit /b 1
)
echo USB tunnel ready: phone localhost:26760 -> Windows localhost:26760
