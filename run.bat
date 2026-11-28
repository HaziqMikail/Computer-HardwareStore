@echo off
rem Builds and runs the store (see run.ps1).
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run.ps1" %*
