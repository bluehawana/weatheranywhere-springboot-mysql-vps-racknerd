#!/bin/bash
# Quick deployment script for WeatherAnywhere updates
# Usage: ./deploy-update.sh <vps-user> <vps-host>

set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <vps-user> <vps-host>"
    echo "Example: $0 ubuntu 203.0.113.45"
    exit 1
fi

VPS_USER=$1
VPS_HOST=$2
APP_DIR="/opt/weatheranywhere"
JAR_FILE="target/EasyWeather-0.0.1-SNAPSHOT.jar"

echo "=== WeatherAnywhere Quick Update Deployment ==="
echo "VPS: ${VPS_USER}@${VPS_HOST}"
echo "JAR: ${JAR_FILE}"
echo ""

# Check if JAR exists
if [ ! -f "${JAR_FILE}" ]; then
    echo "ERROR: JAR file not found: ${JAR_FILE}"
    echo "Please run: mvn clean package"
    exit 1
fi

echo "Step 1: Uploading JAR to VPS..."
scp "${JAR_FILE}" "${VPS_USER}@${VPS_HOST}:/tmp/weatheranywhere.jar"

echo ""
echo "Step 2: Deploying on VPS..."
ssh "${VPS_USER}@${VPS_HOST}" << 'ENDSSH'
    # Stop the service
    echo "Stopping weatheranywhere service..."
    sudo systemctl stop weatheranywhere || true
    
    # Backup current JAR
    if [ -f /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar ]; then
        echo "Backing up current JAR..."
        sudo cp /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar \
                /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar.backup.$(date +%Y%m%d_%H%M%S)
    fi
    
    # Move new JAR
    echo "Installing new JAR..."
    sudo mv /tmp/weatheranywhere.jar /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar
    sudo chown weatherapp:weatherapp /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar
    
    # Start the service
    echo "Starting weatheranywhere service..."
    sudo systemctl start weatheranywhere
    
    # Wait a moment for startup
    sleep 3
    
    # Check status
    echo ""
    echo "Service status:"
    sudo systemctl status weatheranywhere --no-pager
    
    echo ""
    echo "Recent logs:"
    sudo journalctl -u weatheranywhere -n 20 --no-pager
ENDSSH

echo ""
echo "=== Deployment Complete! ==="
echo ""
echo "Check your application at: http://${VPS_HOST}"
echo ""
echo "To view logs: ssh ${VPS_USER}@${VPS_HOST} 'sudo journalctl -u weatheranywhere -f'"
