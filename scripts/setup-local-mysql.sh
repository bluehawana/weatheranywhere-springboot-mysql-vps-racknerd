#!/bin/bash

# ==============================
# Local MySQL Setup Script for WeatherAnywhere
# ==============================

echo "=========================================="
echo "Setting up Local MySQL Database"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}Error: MySQL is not installed or not in PATH${NC}"
    exit 1
fi

echo -e "${GREEN}✓ MySQL found: $(mysql --version)${NC}"
echo ""

# Check if MySQL service is running
echo "Checking MySQL service status..."
if sudo service mysql status > /dev/null 2>&1; then
    echo -e "${GREEN}✓ MySQL service is running${NC}"
else
    echo -e "${YELLOW}MySQL service is not running. Starting it...${NC}"
    sudo service mysql start
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ MySQL service started successfully${NC}"
    else
        echo -e "${RED}✗ Failed to start MySQL service${NC}"
        exit 1
    fi
fi
echo ""

# Run the SQL setup script
echo "Creating database and tables..."
echo "Please enter your MySQL root password when prompted:"
mysql -u root -p <<EOF
CREATE DATABASE IF NOT EXISTS weatheranywhere;
USE weatheranywhere;

CREATE TABLE IF NOT EXISTS cities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE
);

-- Check if index exists before creating it
SET @exists := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = 'weatheranywhere'
    AND table_name = 'cities'
    AND index_name = 'idx_city_name'
);

SET @sql := IF(@exists = 0,
  'CREATE INDEX idx_city_name ON cities(name)',
  'SELECT "Index already exists"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo -e "✓ Database setup completed successfully!"
    echo -e "==========================================${NC}"
    echo ""
    echo "Database: weatheranywhere"
    echo "Connection: jdbc:mysql://localhost:3306/weatheranywhere"
    echo "Username: root"
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo "1. Restart your Spring Boot application"
    echo "2. Test the weather endpoint: http://localhost:8081/weather?city=Paris"
    echo ""
else
    echo ""
    echo -e "${RED}=========================================="
    echo -e "✗ Database setup failed"
    echo -e "==========================================${NC}"
    echo ""
    echo "Please check:"
    echo "1. MySQL root password is correct"
    echo "2. MySQL service is running: sudo service mysql status"
    echo "3. You have permissions to create databases"
    exit 1
fi
