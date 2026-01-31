@echo off
echo Checking environment...

where jpackage >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: 'jpackage' is not found in your PATH.
    echo Please ensure you have JDK 14 or later installed and added to your PATH.
    echo Current JAVA_HOME: %JAVA_HOME%
    pause
    exit /b 1
)

echo Found jpackage. Starting build process...
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "package_windows.ps1"

if %errorlevel% neq 0 (
    echo.
    echo Build failed with error code %errorlevel%.
    pause
    exit /b 1
)

echo.
echo Build successful!
pause
