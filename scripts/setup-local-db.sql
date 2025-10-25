-- ==============================
-- Local MySQL Database Setup for WeatherAnywhere
-- ==============================

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS weatheranywhere;

-- Use the database
USE weatheranywhere;

-- Create cities table
CREATE TABLE IF NOT EXISTS cities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create index on city name for faster lookups
CREATE INDEX IF NOT EXISTS idx_city_name ON cities(name);

-- Insert some test cities
INSERT IGNORE INTO cities (name, latitude, longitude) VALUES
    ('Miami', 25.7617, -80.1918),
    ('Paris', 48.8566, 2.3522),
    ('New York', 40.7128, -74.0060),
    ('London', 51.5074, -0.1278),
    ('Tokyo', 35.6762, 139.6503),
    ('Sydney', -33.8688, 151.2093),
    ('Berlin', 52.5200, 13.4050),
    ('Rome', 41.9028, 12.4964);

-- Show created tables
SHOW TABLES;

-- Show inserted data
SELECT * FROM cities;

-- Success message
SELECT 'Database setup completed successfully!' AS status;
