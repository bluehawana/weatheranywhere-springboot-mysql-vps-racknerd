#!/bin/bash
# WeatherAnywhere - RackNerd VPS Deployment Script
# This script sets up the complete environment on Ubuntu/Debian

set -e

echo "=== WeatherAnywhere VPS Deployment Setup ==="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="weatheranywhere"
APP_USER="weatherapp"
APP_DIR="/opt/weatheranywhere"
LOG_DIR="/var/log/weatheranywhere"
BACKUP_DIR="/var/backups/weatheranywhere"

echo -e "${GREEN}Step 1: System Update${NC}"
sudo apt update
sudo apt upgrade -y

echo ""
echo -e "${GREEN}Step 2: Install Required Packages${NC}"
sudo apt install -y \
  openjdk-17-jdk \
  maven \
  mysql-server \
  nginx \
  git \
  curl \
  unzip

echo ""
echo -e "${GREEN}Step 3: Verify Java Installation${NC}"
java -version
mvn -version

echo ""
echo -e "${GREEN}Step 4: Create Application User${NC}"
if id "${APP_USER}" &>/dev/null; then
  echo "User ${APP_USER} already exists"
else
  sudo useradd -r -m -s /bin/bash ${APP_USER}
  echo "Created user: ${APP_USER}"
fi

echo ""
echo -e "${GREEN}Step 5: Create Application Directories${NC}"
sudo mkdir -p ${APP_DIR}
sudo mkdir -p ${LOG_DIR}
sudo mkdir -p ${BACKUP_DIR}
sudo chown -R ${APP_USER}:${APP_USER} ${APP_DIR}
sudo chown -R ${APP_USER}:${APP_USER} ${LOG_DIR}
sudo chown -R ${APP_USER}:${APP_USER} ${BACKUP_DIR}

echo ""
echo -e "${GREEN}Step 6: Setup MySQL Database${NC}"
echo "MySQL will now be configured..."
sudo mysql_secure_installation

echo ""
echo -e "${YELLOW}Creating database and user...${NC}"
sudo mysql -u root -p << 'MYSQL_SCRIPT'
CREATE DATABASE IF NOT EXISTS weatheranywhere CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'weatherapp'@'localhost' IDENTIFIED BY 'CHANGE_THIS_PASSWORD';
GRANT ALL PRIVILEGES ON weatheranywhere.* TO 'weatherapp'@'localhost';
FLUSH PRIVILEGES;
SELECT user, host FROM mysql.user WHERE user='weatherapp';
SHOW DATABASES LIKE 'weatheranywhere';
MYSQL_SCRIPT

echo ""
echo -e "${GREEN}Step 7: Clone/Copy Application${NC}"
echo "Please copy your application files to: ${APP_DIR}"
echo "Or provide git repository URL:"
read -p "Git repository URL (or press Enter to skip): " GIT_REPO

if [ ! -z "$GIT_REPO" ]; then
  sudo -u ${APP_USER} git clone ${GIT_REPO} ${APP_DIR}
else
  echo "Skipping git clone. Please manually copy files to ${APP_DIR}"
fi

echo ""
echo -e "${GREEN}Step 8: Setup Complete!${NC}"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Copy your application files to: ${APP_DIR}"
echo "2. Create .env file in: ${APP_DIR}/.env"
echo "3. Update database password in MySQL (shown above)"
echo "4. Restore database: ./restore-database.sh backup-file.sql"
echo "5. Build application: cd ${APP_DIR} && mvn clean package"
echo "6. Install systemd service: sudo cp weatheranywhere.service /etc/systemd/system/"
echo "7. Configure Nginx: sudo cp nginx-weatheranywhere.conf /etc/nginx/sites-available/"
echo "8. Start application: sudo systemctl start weatheranywhere"
echo ""
echo -e "${GREEN}Installation directory: ${APP_DIR}${NC}"
echo -e "${GREEN}Log directory: ${LOG_DIR}${NC}"
echo -e "${GREEN}Backup directory: ${BACKUP_DIR}${NC}"
