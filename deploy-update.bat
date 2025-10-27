@echo off
REM Quick deployment script for WeatherAnywhere updates (Windows)
REM Usage: deploy-update.bat <vps-user> <vps-host>

if "%~2"=="" (
    echo Usage: %0 ^<vps-user^> ^<vps-host^>
    echo Example: %0 ubuntu 203.0.113.45
    exit /b 1
)

set VPS_USER=%1
set VPS_HOST=%2
set APP_DIR=/opt/weatheranywhere
set JAR_FILE=target\EasyWeather-0.0.1-SNAPSHOT.jar

echo === WeatherAnywhere Quick Update Deployment ===
echo VPS: %VPS_USER%@%VPS_HOST%
echo JAR: %JAR_FILE%
echo.

REM Check if JAR exists
if not exist "%JAR_FILE%" (
    echo ERROR: JAR file not found: %JAR_FILE%
    echo Please run: mvn clean package
    exit /b 1
)

echo Step 1: Uploading JAR to VPS...
scp "%JAR_FILE%" "%VPS_USER%@%VPS_HOST%:/tmp/weatheranywhere.jar"

echo.
echo Step 2: Deploying on VPS...
ssh "%VPS_USER%@%VPS_HOST%" "sudo systemctl stop weatheranywhere && sudo mv /tmp/weatheranywhere.jar /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar && sudo chown weatherapp:weatherapp /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar && sudo systemctl start weatheranywhere && sleep 3 && sudo systemctl status weatheranywhere"

echo.
echo === Deployment Complete! ===
echo.
echo Check your application at: http://%VPS_HOST%
echo.
pause
