#!/bin/bash
# MySQL Database Setup for WeatherAnywhere
# Run this script on your RackNerd VPS

set -e

echo "=== WeatherAnywhere MySQL Database Setup ==="
echo ""

# Database configuration
DB_NAME="weatheranywhere"
DB_USER="weatherapp"

# Prompt for password
echo "Enter a secure password for the database user '${DB_USER}':"
read -s DB_PASSWORD
echo ""
echo "Confirm password:"
read -s DB_PASSWORD_CONFIRM
echo ""

if [ "$DB_PASSWORD" != "$DB_PASSWORD_CONFIRM" ]; then
  echo "Passwords do not match!"
  exit 1
fi

echo "Creating database and user..."

# Create database and user
sudo mysql -u root -p << MYSQL_SCRIPT
-- Create database
CREATE DATABASE IF NOT EXISTS ${DB_NAME}
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost'
  IDENTIFIED BY '${DB_PASSWORD}';

-- Grant privileges
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;

-- Show created user
SELECT user, host FROM mysql.user WHERE user='${DB_USER}';

-- Show databases
SHOW DATABASES LIKE '${DB_NAME}';

-- Use the database
USE ${DB_NAME};

-- Show tables (should be empty initially)
SHOW TABLES;
MYSQL_SCRIPT

if [ $? -eq 0 ]; then
  echo ""
  echo "✓ Database setup complete!"
  echo ""
  echo "Database details:"
  echo "  Name: ${DB_NAME}"
  echo "  User: ${DB_USER}"
  echo "  Host: localhost"
  echo ""
  echo "Update your .env file with:"
  echo "  spring.datasource.url=jdbc:mysql://localhost:3306/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  echo "  spring.datasource.username=${DB_USER}"
  echo "  spring.datasource.password=${DB_PASSWORD}"
  echo ""
  echo "Next steps:"
  echo "1. Update .env file with the password above"
  echo "2. Restore your database backup (if you have one)"
  echo "3. Build and start the application"
else
  echo "✗ Database setup failed!"
  exit 1
fi
