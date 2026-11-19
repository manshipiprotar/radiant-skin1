@echo off
title Radiant Skin - Java Backend Server
echo ========================================================
echo    Starting Radiant Skin Java Backend Server...
echo ========================================================

cd /d "%~dp0"

set "JAVA_CMD=java"
set "JAVAC_CMD=javac"

where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\JetBrains\PyCharm Community Edition 2023.2.5\jbr\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\JetBrains\PyCharm Community Edition 2023.2.5\jbr\bin\javac.exe"
        set "JAVA_CMD=C:\Program Files\JetBrains\PyCharm Community Edition 2023.2.5\jbr\bin\java.exe"
    )
)

echo Compiling App.java...
"%JAVAC_CMD%" -encoding UTF-8 -cp "lib/*;bin" -d bin src/App.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo Starting Server on http://localhost:8080 ...
"%JAVA_CMD%" -cp "bin;lib/*" App
pause
