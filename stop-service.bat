@echo off
echo Parando InkFlow API...
taskkill /f /im java.exe /fi "WINDOWTITLE eq InkFlow API"
echo InkFlow API parado!
pause