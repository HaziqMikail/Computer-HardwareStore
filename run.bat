@echo off
setlocal

:: Find Java
where java >nul 2>nul
if %errorlevel% equ 0 (
    set "JAVA_EXE=java"
    goto :run
)

if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_EXE=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    goto :run
)

if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    goto :run
)

echo [ERROR] Java could not be found. Please ensure Java is installed or added to PATH.
pause
exit /b 1

:run
echo Starting Computer Hardware Store...
start "" "%JAVA_EXE%" -jar "%~dp0dist\ComputerHardwareStore.jar"

