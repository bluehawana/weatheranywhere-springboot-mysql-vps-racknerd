# Local MySQL Database Setup Guide

## Overview
This guide helps you set up a local MySQL database for the WeatherAnywhere application instead of using the remote JawsDB instance.

## Prerequisites
- MySQL Server installed (✓ You have MySQL 8.0.43)
- MySQL root password

## Quick Setup

### Option 1: Using the Automated Script (Recommended)

```bash
# Make sure you're in the project directory
cd /mnt/c/Users/BLUEH/projects/weatheranywhere

# Run the setup script
./scripts/setup-local-mysql.sh
```

The script will:
1. Check if MySQL is installed
2. Start MySQL service if not running
3. Create the `weatheranywhere` database
4. Create the `cities` table
5. Insert test data for 8 cities

### Option 2: Manual Setup

If you prefer to set up manually:

```bash
# 1. Start MySQL service
sudo service mysql start

# 2. Login to MySQL
mysql -u root -p

# 3. Run these SQL commands:
CREATE DATABASE IF NOT EXISTS weatheranywhere;
USE weatheranywhere;

CREATE TABLE IF NOT EXISTS cities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO cities (name, latitude, longitude) VALUES
    ('Miami', 25.7617, -80.1918),
    ('Paris', 48.8566, 2.3522),
    ('New York', 40.7128, -74.0060),
    ('London', 51.5074, -0.1278);

EXIT;
```

## Configuration Updated

The `application.properties` file has been updated with:

```properties
# Local MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/weatheranywhere?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=rootadmin

# OpenCage API Key (for geocoding)
opencage.api.key=YOUR_OPENCAGE_API_KEY
```

## After Setup

1. **Restart Spring Boot Application:**
   ```bash
   # Kill existing process if running
   pkill -f "spring-boot:run"

   # Start fresh
   mvn spring-boot:run
   ```

2. **Test the Application:**
   ```bash
   # Test with a city
   curl "http://localhost:8081/weather?city=Paris"

   # Or open in browser
   http://localhost:8081/
   ```

3. **Verify Database Connection:**
   ```bash
   mysql -u root -p weatheranywhere -e "SELECT * FROM cities;"
   ```

## Troubleshooting

### MySQL Service Not Starting
```bash
# Check status
sudo service mysql status

# Check logs
sudo tail -f /var/log/mysql/error.log
```

### Connection Refused
- Ensure MySQL is running: `sudo service mysql start`
- Verify port 3306 is open: `sudo netstat -tlnp | grep 3306`
- Check credentials in application.properties

### Access Denied
- Verify root password in application.properties matches your MySQL root password
- Update password in: `src/main/resources/application.properties` line 17

## Database Schema

### Cities Table
| Column | Type | Description |
|--------|------|-------------|
| id | INT | Auto-increment primary key |
| name | VARCHAR(255) | City name (unique) |
| latitude | DOUBLE | Latitude coordinate |
| longitude | DOUBLE | Longitude coordinate |
| created_at | TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | Last update time |

## Test Cities Included

The setup includes these pre-configured cities:
- Miami (25.7617, -80.1918)
- Paris (48.8566, 2.3522)
- New York (40.7128, -74.0060)
- London (51.5074, -0.1278)
- Tokyo (35.6762, 139.6503)
- Sydney (-33.8688, 151.2093)
- Berlin (52.5200, 13.4050)
- Rome (41.9028, 12.4964)

## Switching Back to Remote Database

To use the remote JawsDB database instead:

Edit `application.properties` and change:
```properties
spring.datasource.url=jdbc:mysql://nba02whlntki5w2p.cbetxkdyhwsb.us-east-1.rds.amazonaws.com:3306/r6rrjlqt6d8haf12?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```
